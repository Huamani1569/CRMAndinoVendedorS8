package com.crmandino.vendedor.data.repository

import androidx.room.withTransaction
import com.crmandino.vendedor.data.local.AppDatabase
import com.crmandino.vendedor.data.local.SesionLocal
import com.crmandino.vendedor.data.local.entities.LeadEntity
import com.crmandino.vendedor.data.local.entities.TareaEntity
import com.crmandino.vendedor.data.mapper.aModelo
import com.crmandino.vendedor.data.mapper.aRequest
import com.crmandino.vendedor.data.remote.dto.EtapaRequest
import com.crmandino.vendedor.model.EntidadSync
import com.crmandino.vendedor.model.EstadoSincronizacion
import com.crmandino.vendedor.model.EtapaVenta
import com.crmandino.vendedor.model.Lead
import com.crmandino.vendedor.model.TipoOperacion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

// Datos que el vendedor escribe en el formulario del lead.
data class DatosLead(
    val empresa: String,
    val contacto: String,
    val telefono: String?,
    val correo: String?,
    val ruc: String?,
    val direccion: String?,
    val latitud: Double?,
    val longitud: Double?,
    val valor: Double
)

// Repositorio de LEADS: las pantallas leen de Room; cada escritura guarda el dato + su operación
// pendiente en una transacción y luego pide sincronizar (si no hay internet, queda en cola).
class LeadRepository(
    private val db: AppDatabase,
    private val sesion: SesionLocal,
    private val cola: ColaOperaciones,
    private val programarSync: () -> Unit
) {
    private val leadDao = db.leadDao()
    private val tareaDao = db.tareaDao()

    fun observarLeads(): Flow<List<Lead>> =
        leadDao.obtenerTodos().map { lista -> lista.map { it.aModelo() } }

    fun observarPorEtapa(etapa: String?): Flow<List<Lead>> =
        (if (etapa == null) leadDao.obtenerTodos() else leadDao.obtenerPorEtapa(etapa))
            .map { lista -> lista.map { it.aModelo() } }

    fun observarLead(id: Int): Flow<Lead?> =
        leadDao.observarPorId(id).map { it?.takeIf { lead -> !lead.eliminado }?.aModelo() }

    suspend fun leadPorUuid(uuid: String): Lead? = leadDao.porUuid(uuid)?.aModelo()

    suspend fun crearLead(d: DatosLead) {
        val usuario = sesion.usuario() ?: return
        db.withTransaction {
            val lead = LeadEntity(
                uuid = UUID.randomUUID().toString(),
                empresa = d.empresa, contacto = d.contacto, telefono = d.telefono, correo = d.correo, ruc = d.ruc,
                direccion = d.direccion, latitud = d.latitud, longitud = d.longitud,
                valorPotencial = d.valor,
                etapa = EtapaVenta.NUEVO.name,
                vendedorId = usuario.id,
                vendedorNombre = usuario.nombre
            )
            val idLocal = leadDao.insertar(lead).toInt()
            cola.registrar(EntidadSync.LEAD, idLocal, TipoOperacion.CREAR, lead.aRequest(), "Crear lead \"${d.empresa}\"")
        }
        programarSync()
    }

    suspend fun actualizarLead(id: Int, d: DatosLead) {
        db.withTransaction {
            val actual = leadDao.porId(id) ?: return@withTransaction
            val editado = actual.copy(
                empresa = d.empresa, contacto = d.contacto, telefono = d.telefono, correo = d.correo, ruc = d.ruc,
                direccion = d.direccion, latitud = d.latitud ?: actual.latitud, longitud = d.longitud ?: actual.longitud,
                valorPotencial = d.valor,
                estadoSync = EstadoSincronizacion.PENDIENTE.name,
                mensajeError = null,
                actualizadoEnLocal = System.currentTimeMillis()
            )
            leadDao.actualizar(editado)
            cola.registrar(EntidadSync.LEAD, id, TipoOperacion.ACTUALIZAR, editado.aRequest(), "Editar lead \"${d.empresa}\"")
        }
        programarSync()
    }

    // Cambia la etapa (PUT /api/leads/{id}/etapa) y, si se pide, crea un recordatorio:
    // todo en UNA transacción (las operaciones quedan juntas en la cola).
    suspend fun cambiarEtapa(id: Int, nuevaEtapa: EtapaVenta, motivoPerdida: String?, recordatorio: String?, fechaLimite: String?) {
        db.withTransaction {
            val actual = leadDao.porId(id) ?: return@withTransaction
            val anterior = EtapaVenta.desdeTexto(actual.etapa)
            val editado = actual.copy(
                etapa = nuevaEtapa.name,
                motivoPerdida = if (nuevaEtapa == EtapaVenta.PERDIDO) motivoPerdida else actual.motivoPerdida,
                estadoSync = EstadoSincronizacion.PENDIENTE.name,
                mensajeError = null,
                actualizadoEnLocal = System.currentTimeMillis()
            )
            leadDao.actualizar(editado)
            cola.registrar(
                EntidadSync.LEAD, id, TipoOperacion.ETAPA, EtapaRequest(nuevaEtapa.name, motivoPerdida),
                "Etapa de \"${actual.empresa}\": ${anterior.etiqueta} → ${nuevaEtapa.etiqueta}"
            )
            if (!recordatorio.isNullOrBlank() && !fechaLimite.isNullOrBlank()) {
                val tarea = TareaEntity(
                    uuid = UUID.randomUUID().toString(),
                    leadUuid = actual.uuid,
                    leadEmpresa = actual.empresa,
                    descripcion = recordatorio,
                    fechaLimite = fechaLimite,
                    vendedorId = actual.vendedorId,
                    vendedorNombre = actual.vendedorNombre
                )
                val idTarea = tareaDao.insertar(tarea).toInt()
                cola.registrar(EntidadSync.TAREA, idTarea, TipoOperacion.CREAR, tarea.aRequest(), "Recordatorio: $recordatorio")
            }
        }
        programarSync()
    }

    // Al registrar una visita o cotización, el lead se actualiza en el celular sin esperar al servidor.
    suspend fun marcarEtapaLocal(uuid: String, etapa: EtapaVenta) {
        val actual = leadDao.porUuid(uuid) ?: return
        if (EtapaVenta.desdeTexto(actual.etapa).abierta) leadDao.actualizar(actual.copy(etapa = etapa.name, diasSinContacto = 0))
    }
}
