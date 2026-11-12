package com.crmandino.vendedor.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.crmandino.vendedor.data.local.entities.CacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CacheDao {
    @Query("SELECT * FROM cache_json WHERE clave = :clave")
    fun observar(clave: String): Flow<CacheEntity?>

    @Query("SELECT * FROM cache_json WHERE clave = :clave")
    suspend fun leer(clave: String): CacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(cache: CacheEntity)

    @Query("DELETE FROM cache_json")
    suspend fun borrarTodo()
}
