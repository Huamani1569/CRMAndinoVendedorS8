package com.crmandino.vendedor.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.crmandino.vendedor.data.local.entities.TareaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TareaDao {

    @Insert
    suspend fun insertar(tarea: TareaEntity): Long

    @Update
    suspend fun actualizar(tarea: TareaEntity)

    @Delete
    suspend fun eliminar(tarea: TareaEntity)

    @Query("SELECT * FROM tareas WHERE eliminado = 0 ORDER BY id DESC")
    fun obtenerTodos(): Flow<List<TareaEntity>>

    @Query("SELECT * FROM tareas WHERE eliminado = 0 AND completada = 0 ORDER BY substr(fechaLimite, 7, 4), substr(fechaLimite, 4, 2), substr(fechaLimite, 1, 2)")
    fun obtenerPendientes(): Flow<List<TareaEntity>>

    @Query("SELECT * FROM tareas WHERE leadUuid = :leadUuid AND eliminado = 0 ORDER BY id DESC")
    fun obtenerPorLead(leadUuid: String): Flow<List<TareaEntity>>

    @Query("SELECT * FROM tareas WHERE id = :id")
    suspend fun porId(id: Int): TareaEntity?

    @Query("SELECT * FROM tareas WHERE uuid = :uuid")
    suspend fun porUuid(uuid: String): TareaEntity?

    @Query("SELECT * FROM tareas")
    suspend fun todos(): List<TareaEntity>

    @Query("UPDATE tareas SET estadoSync = :estado, mensajeError = :mensaje WHERE id = :id")
    suspend fun cambiarEstado(id: Int, estado: String, mensaje: String?)

    @Query("DELETE FROM tareas")
    suspend fun borrarTodo()
}
