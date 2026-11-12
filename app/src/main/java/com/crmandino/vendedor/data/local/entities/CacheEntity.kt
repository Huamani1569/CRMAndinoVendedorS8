package com.crmandino.vendedor.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

// Copia local (JSON) de datos de solo lectura: catálogo, cotizaciones, ruta del día,
// fichas de clientes, recomendaciones y notificaciones. Así se pueden ver sin conexión.
@Entity(tableName = "cache_json")
data class CacheEntity(
    @PrimaryKey val clave: String,
    val json: String,
    val actualizadoEn: Long = System.currentTimeMillis()
)
