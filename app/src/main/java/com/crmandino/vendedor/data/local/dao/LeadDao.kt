package com.crmandino.vendedor.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.crmandino.vendedor.data.local.entities.LeadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LeadDao {

    @Insert
    suspend fun insertar(lead: LeadEntity): Long

    @Update
    suspend fun actualizar(lead: LeadEntity)

    @Delete
    suspend fun eliminar(lead: LeadEntity)

    // RF06: los leads se leen siempre desde Room (funciona sin conexión)
    @Query("SELECT * FROM leads WHERE eliminado = 0 ORDER BY empresa")
    fun obtenerTodos(): Flow<List<LeadEntity>>

    // RF07: filtro por etapa del embudo
    @Query("SELECT * FROM leads WHERE eliminado = 0 AND etapa = :etapa ORDER BY empresa")
    fun obtenerPorEtapa(etapa: String): Flow<List<LeadEntity>>

    @Query("SELECT * FROM leads WHERE id = :id")
    fun observarPorId(id: Int): Flow<LeadEntity?>

    @Query("SELECT * FROM leads WHERE id = :id")
    suspend fun porId(id: Int): LeadEntity?

    @Query("SELECT * FROM leads WHERE uuid = :uuid")
    suspend fun porUuid(uuid: String): LeadEntity?

    @Query("SELECT * FROM leads")
    suspend fun todos(): List<LeadEntity>

    @Query("UPDATE leads SET estadoSync = :estado, mensajeError = :mensaje WHERE id = :id")
    suspend fun cambiarEstado(id: Int, estado: String, mensaje: String?)

    @Query("DELETE FROM leads")
    suspend fun borrarTodo()
}
