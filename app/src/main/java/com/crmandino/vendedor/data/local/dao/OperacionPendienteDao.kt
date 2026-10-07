package com.crmandino.vendedor.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.crmandino.vendedor.data.local.entities.OperacionPendienteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OperacionPendienteDao {

    @Insert
    suspend fun insertar(operacion: OperacionPendienteEntity): Long

    @Update
    suspend fun actualizar(operacion: OperacionPendienteEntity)

    @Delete
    suspend fun eliminar(operacion: OperacionPendienteEntity)

    // Toda la cola (pendientes, errores e historial reciente) para la pantalla de sincronización
    @Query("SELECT * FROM operaciones_pendientes ORDER BY id DESC")
    fun observarTodas(): Flow<List<OperacionPendienteEntity>>

    // Operaciones a enviar, en el mismo orden en que se hicieron (FIFO)
    @Query("SELECT * FROM operaciones_pendientes WHERE estado = 'PENDIENTE' ORDER BY id")
    suspend fun pendientesParaEnviar(): List<OperacionPendienteEntity>

    @Query("SELECT COUNT(*) FROM operaciones_pendientes WHERE estado IN ('PENDIENTE', 'ENVIANDO')")
    fun contarPendientes(): Flow<Int>

    @Query("SELECT COUNT(*) FROM operaciones_pendientes WHERE estado = 'ERROR'")
    fun contarErrores(): Flow<Int>

    @Query("SELECT COUNT(*) FROM operaciones_pendientes WHERE estado IN ('PENDIENTE', 'ENVIANDO', 'ERROR')")
    suspend fun contarSinSincronizar(): Int

    // ¿Le quedan operaciones sin confirmar a este registro?
    @Query(
        "SELECT COUNT(*) FROM operaciones_pendientes " +
            "WHERE entidad = :entidad AND idEntidadLocal = :idLocal AND estado IN ('PENDIENTE', 'ENVIANDO', 'ERROR')"
    )
    suspend fun contarActivasDe(entidad: String, idLocal: Int): Int

    @Query("SELECT * FROM operaciones_pendientes WHERE id = :id")
    suspend fun porId(id: Int): OperacionPendienteEntity?

    // Si la app se cerró a mitad de un envío, esas operaciones vuelven a PENDIENTE
    @Query("UPDATE operaciones_pendientes SET estado = 'PENDIENTE' WHERE estado = 'ENVIANDO'")
    suspend fun reiniciarEnviando()

    // Se conserva solo el historial reciente de operaciones ya sincronizadas
    @Query(
        "DELETE FROM operaciones_pendientes WHERE estado = 'SINCRONIZADO' AND id NOT IN " +
            "(SELECT id FROM operaciones_pendientes WHERE estado = 'SINCRONIZADO' ORDER BY id DESC LIMIT 40)"
    )
    suspend fun limpiarHistorial()

    @Query("DELETE FROM operaciones_pendientes")
    suspend fun borrarTodo()
}
