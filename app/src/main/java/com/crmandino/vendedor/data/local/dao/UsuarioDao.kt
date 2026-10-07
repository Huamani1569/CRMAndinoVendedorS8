package com.crmandino.vendedor.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.crmandino.vendedor.data.local.entities.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Upsert
    suspend fun guardar(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuarios WHERE esSesion = 1 LIMIT 1")
    fun observarSesion(): Flow<UsuarioEntity?>

    @Query("DELETE FROM usuarios")
    suspend fun borrarTodo()
}
