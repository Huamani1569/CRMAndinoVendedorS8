package com.crmandino.vendedor.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.crmandino.vendedor.data.local.entities.InteraccionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InteraccionDao {

    @Insert
    suspend fun insertar(interaccion: InteraccionEntity): Long

    @Update
    suspend fun actualizar(interaccion: InteraccionEntity)

    @Delete
    suspend fun eliminar(interaccion: InteraccionEntity)

    @Query("SELECT * FROM interacciones WHERE eliminado = 0 ORDER BY id DESC")
    fun obtenerTodos(): Flow<List<InteraccionEntity>>

    @Query("SELECT * FROM interacciones WHERE leadUuid = :leadUuid AND eliminado = 0 ORDER BY id DESC")
    fun obtenerPorLead(leadUuid: String): Flow<List<InteraccionEntity>>

    @Query("SELECT * FROM interacciones WHERE id = :id")
    suspend fun porId(id: Int): InteraccionEntity?

    @Query("SELECT * FROM interacciones WHERE uuid = :uuid")
    suspend fun porUuid(uuid: String): InteraccionEntity?

    @Query("SELECT * FROM interacciones")
    suspend fun todos(): List<InteraccionEntity>

    @Query("UPDATE interacciones SET estadoSync = :estado, mensajeError = :mensaje WHERE id = :id")
    suspend fun cambiarEstado(id: Int, estado: String, mensaje: String?)

    @Query("DELETE FROM interacciones")
    suspend fun borrarTodo()
}
