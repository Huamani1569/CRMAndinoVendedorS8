package com.crmandino.vendedor.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

// Tabla sync_metadata: cuándo se sincronizó con éxito cada recurso y el cursor del servidor.
@Entity(tableName = "sync_metadata")
data class SyncMetadataEntity(
    @PrimaryKey val recurso: String,            // "general", "leads", "interacciones", "tareas"
    val ultimaSincronizacionExitosa: Long? = null,
    val cursorRemoto: String? = null            // hora del servidor en la última descarga
)
