package com.crmandino.vendedor.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.crmandino.vendedor.model.EstadoSincronizacion

// Tabla leads en Room (copia local de los leads del vendedor).
// Además de los datos del negocio guarda los datos de SINCRONIZACIÓN:
// id local, id remoto, uuid, versión y estado (PENDIENTE, ENVIANDO, SINCRONIZADO, ERROR).
@Entity(
    tableName = "leads",
    indices = [Index(value = ["uuid"], unique = true), Index(value = ["remoteId"])]
)
data class LeadEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,   // identificador LOCAL
    val remoteId: Int? = null,                          // identificador REMOTO (id en la BD de Neon)
    val uuid: String,                                   // identificador global (evita duplicados)
    val empresa: String,
    val contacto: String,
    val valorPotencial: Double,
    val etapa: String,
    val vendedorId: Int,
    val vendedorNombre: String,
    val version: Int = 0,                               // versión confirmada por el servidor (0 = aún no existe allá)
    val estadoSync: String = EstadoSincronizacion.PENDIENTE.name,
    val mensajeError: String? = null,
    val eliminado: Boolean = false,
    val actualizadoEnLocal: Long = System.currentTimeMillis()
)
