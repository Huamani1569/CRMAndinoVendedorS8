package com.crmandino.vendedor.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.crmandino.vendedor.data.local.dao.InteraccionDao
import com.crmandino.vendedor.data.local.dao.LeadDao
import com.crmandino.vendedor.data.local.dao.OperacionPendienteDao
import com.crmandino.vendedor.data.local.dao.SyncMetadataDao
import com.crmandino.vendedor.data.local.dao.TareaDao
import com.crmandino.vendedor.data.local.dao.UsuarioDao
import com.crmandino.vendedor.data.local.entities.InteraccionEntity
import com.crmandino.vendedor.data.local.entities.LeadEntity
import com.crmandino.vendedor.data.local.entities.OperacionPendienteEntity
import com.crmandino.vendedor.data.local.entities.SyncMetadataEntity
import com.crmandino.vendedor.data.local.entities.TareaEntity
import com.crmandino.vendedor.data.local.entities.UsuarioEntity

// Base de datos local (SQLite) de la app Vendedor: copia de los datos + cola de sincronización.
@Database(
    entities = [
        UsuarioEntity::class,
        LeadEntity::class,
        InteraccionEntity::class,
        TareaEntity::class,
        OperacionPendienteEntity::class,
        SyncMetadataEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun leadDao(): LeadDao
    abstract fun interaccionDao(): InteraccionDao
    abstract fun tareaDao(): TareaDao
    abstract fun operacionPendienteDao(): OperacionPendienteDao
    abstract fun syncMetadataDao(): SyncMetadataDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDB(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "crm_vendedor_s8.db"   // nombre nuevo: no choca con la base de la Semana 6
                ).build().also { INSTANCE = it }
            }
    }
}
