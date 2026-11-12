package com.crmandino.vendedor.data.repository

import androidx.room.withTransaction
import com.crmandino.vendedor.data.local.AppDatabase
import com.crmandino.vendedor.data.local.entities.InteraccionEntity
import com.crmandino.vendedor.data.mapper.aModelo
import com.crmandino.vendedor.data.mapper.aRequest
import com.crmandino.vendedor.model.EntidadSync
import com.crmandino.vendedor.model.EstadoSincronizacion
import com.crmandino.vendedor.model.Interaccion
import com.crmandino.vendedor.model.TipoOperacion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

// RF08 / RF-V05: interacciones (llamada, WhatsApp, correo, reunión, visita) con su resultado de un lead, con cola de sincronización.
class InteraccionRepository(
    private val db: AppDatabase,
    private val cola: ColaOperaciones,
    private val programarSync: () -> Unit
) {
    private val dao = db.interaccionDao()

    fun observarPorLead(leadUuid: String): Flow<List<Interaccion>> =
        dao.obtenerPorLead(leadUuid).map { lista -> lista.map { it.aModelo() } }

    suspend fun registrar(leadUuid: String, tipo: String, fecha: String, resultado: String?, nota: String) {
        db.withTransaction {
            val nueva = InteraccionEntity(uuid = UUID.randomUUID().toString(), leadUuid = leadUuid, tipo = tipo, fecha = fecha, resultado = resultado, nota = nota)
            val idLocal = dao.insertar(nueva).toInt()
            cola.registrar(EntidadSync.INTERACCION, idLocal, TipoOperacion.CREAR, nueva.aRequest(), "Registrar $tipo del $fecha")
        }
        // El lead deja de figurar "sin contacto" de inmediato (el servidor recalcula su puntaje al recibirla)
        db.leadDao().porUuid(leadUuid)?.let { db.leadDao().actualizar(it.copy(diasSinContacto = 0)) }
        programarSync()
    }

    suspend fun editar(id: Int, tipo: String, fecha: String, resultado: String?, nota: String) {
        db.withTransaction {
            val actual = dao.porId(id) ?: return@withTransaction
            val editada = actual.copy(tipo = tipo, fecha = fecha, resultado = resultado, nota = nota, estadoSync = EstadoSincronizacion.PENDIENTE.name, mensajeError = null)
            dao.actualizar(editada)
            cola.registrar(EntidadSync.INTERACCION, id, TipoOperacion.ACTUALIZAR, editada.aRequest(), "Editar $tipo del $fecha")
        }
        programarSync()
    }

    // Borrado lógico: se oculta ya, y se borra del celular cuando el servidor lo confirma.
    suspend fun eliminar(id: Int) {
        db.withTransaction {
            val actual = dao.porId(id) ?: return@withTransaction
            dao.actualizar(actual.copy(eliminado = true, estadoSync = EstadoSincronizacion.PENDIENTE.name, mensajeError = null))
            cola.registrar(EntidadSync.INTERACCION, id, TipoOperacion.ELIMINAR, actual.aRequest(), "Eliminar ${actual.tipo} del ${actual.fecha}")
        }
        programarSync()
    }
}
