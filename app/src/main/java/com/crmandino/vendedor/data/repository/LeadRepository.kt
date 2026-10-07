package com.crmandino.vendedor.data.repository

import androidx.room.withTransaction
import com.crmandino.vendedor.data.local.AppDatabase
import com.crmandino.vendedor.data.local.SesionLocal
import com.crmandino.vendedor.data.local.entities.LeadEntity
import com.crmandino.vendedor.data.local.entities.TareaEntity
import com.crmandino.vendedor.data.mapper.aModelo
import com.crmandino.vendedor.data.mapper.aRequest
import com.crmandino.vendedor.model.EntidadSync
import com.crmandino.vendedor.model.EstadoSincronizacion
import com.crmandino.vendedor.model.Lead
import com.crmandino.vendedor.model.TipoOperacion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

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

    suspend fun crearLead(empresa: String, contacto: String, valor: Double, etapa: String) {
        val usuario = sesion.usuario() ?: return
        db.withTransaction {
            val lead = LeadEntity(
                uuid = UUID.randomUUID().toString(),
                empresa = empresa,
                contacto = contacto,
                valorPotencial = valor,
                etapa = etapa,
                vendedorId = usuario.id,
                vendedorNombre = usuario.nombre
            )
            val idLocal = leadDao.insertar(lead).toInt()
            cola.registrar(EntidadSync.LEAD, idLocal, TipoOperacion.CREAR, lead.aRequest(), "Crear lead \"$empresa\"")
        }
        programarSync()
    }

    suspend fun actualizarLead(id: Int, empresa: String, contacto: String, valor: Double, etapa: String) {
        db.withTransaction {
            val actual = leadDao.porId(id) ?: return@withTransaction
            val editado = actual.copy(
                empresa = empresa,
                contacto = contacto,
                valorPotencial = valor,
                etapa = etapa,
                estadoSync = EstadoSincronizacion.PENDIENTE.name,
                mensajeError = null,
                actualizadoEnLocal = System.currentTimeMillis()
            )
            leadDao.actualizar(editado)
            cola.registrar(EntidadSync.LEAD, id, TipoOperacion.ACTUALIZAR, editado.aRequest(), "Editar lead \"$empresa\"")
        }
        programarSync()
    }

    // RF09: cambia la etapa y crea el recordatorio, todo en UNA transacción (2 operaciones en cola).
    suspend fun cambiarEtapaConRecordatorio(id: Int, nuevaEtapa: String, recordatorio: String, fechaLimite: String) {
        db.withTransaction {
            val actual = leadDao.porId(id) ?: return@withTransaction
            val editado = actual.copy(
                etapa = nuevaEtapa,
                estadoSync = EstadoSincronizacion.PENDIENTE.name,
                mensajeError = null,
                actualizadoEnLocal = System.currentTimeMillis()
            )
            leadDao.actualizar(editado)
            cola.registrar(
                EntidadSync.LEAD, id, TipoOperacion.ACTUALIZAR, editado.aRequest(),
                "Cambiar etapa de \"${actual.empresa}\": ${actual.etapa} → $nuevaEtapa"
            )

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
        programarSync()
    }
}
