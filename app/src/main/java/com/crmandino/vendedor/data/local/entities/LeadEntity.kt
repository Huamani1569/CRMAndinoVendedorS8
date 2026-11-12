package com.crmandino.vendedor.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.crmandino.vendedor.model.EstadoSincronizacion

// Tabla leads en Room (copia local de la cartera del vendedor).
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
    val telefono: String? = null,
    val correo: String? = null,
    val ruc: String? = null,
    val direccion: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val valorPotencial: Double,
    val etapa: String,                                  // código de la etapa (NUEVO, CONTACTADO...)
    val canalOrigen: String? = "VENDEDOR",
    val codigoSeguimiento: String? = null,
    val comentarioCliente: String? = null,
    val puntaje: Int? = null,                           // ML-01: probabilidad de cierre (lo calcula el servidor)
    val factoresJson: String? = null,                   // explicación del puntaje (JSON)
    val diasSinContacto: Int? = null,
    val clienteId: Int? = null,
    val motivoPerdida: String? = null,
    val vendedorId: Int,
    val vendedorNombre: String,
    val version: Int = 0,                               // versión confirmada por el servidor (0 = aún no existe allá)
    val estadoSync: String = EstadoSincronizacion.PENDIENTE.name,
    val mensajeError: String? = null,
    val eliminado: Boolean = false,
    val actualizadoEnLocal: Long = System.currentTimeMillis()
)
