package com.crmandino.vendedor.data.repository

import androidx.room.withTransaction
import com.crmandino.vendedor.data.local.AppDatabase
import com.crmandino.vendedor.data.mapper.aModelo
import com.crmandino.vendedor.data.mapper.aRequest
import com.crmandino.vendedor.model.EntidadSync
import com.crmandino.vendedor.model.EstadoSincronizacion
import com.crmandino.vendedor.model.Tarea
import com.crmandino.vendedor.model.TipoOperacion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Recordatorios de seguimiento (tareas con fecha límite).
class TareaRepository(
    private val db: AppDatabase,
    private val cola: ColaOperaciones,
    private val programarSync: () -> Unit
) {
    private val dao = db.tareaDao()

    fun observarPendientes(): Flow<List<Tarea>> =
        dao.obtenerPendientes().map { lista -> lista.map { it.aModelo() } }

    fun observarPorLead(leadUuid: String): Flow<List<Tarea>> =
        dao.obtenerPorLead(leadUuid).map { lista -> lista.map { it.aModelo() } }

    suspend fun cambiarEstado(id: Int) {
        db.withTransaction {
            val actual = dao.porId(id) ?: return@withTransaction
            val editada = actual.copy(completada = !actual.completada, estadoSync = EstadoSincronizacion.PENDIENTE.name, mensajeError = null)
            dao.actualizar(editada)
            val accion = if (editada.completada) "Completar" else "Reabrir"
            cola.registrar(EntidadSync.TAREA, id, TipoOperacion.ACTUALIZAR, editada.aRequest(), "$accion tarea \"${actual.descripcion}\"")
        }
        programarSync()
    }

    suspend fun eliminar(id: Int) {
        db.withTransaction {
            val actual = dao.porId(id) ?: return@withTransaction
            dao.actualizar(actual.copy(eliminado = true, estadoSync = EstadoSincronizacion.PENDIENTE.name, mensajeError = null))
            cola.registrar(EntidadSync.TAREA, id, TipoOperacion.ELIMINAR, actual.aRequest(), "Eliminar tarea \"${actual.descripcion}\"")
        }
        programarSync()
    }
}
