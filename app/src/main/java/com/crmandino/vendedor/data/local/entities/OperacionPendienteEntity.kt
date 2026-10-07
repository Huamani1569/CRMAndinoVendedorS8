package com.crmandino.vendedor.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.crmandino.vendedor.model.EstadoSincronizacion

// COLA LOCAL de operaciones (tabla operaciones_pendientes de la guía).
// Cada escritura (crear, editar, eliminar) se guarda aquí en la MISMA transacción que el dato,
// así nunca se pierde aunque no haya internet o se cierre la app.
@Entity(
    tableName = "operaciones_pendientes",
    indices = [Index(value = ["uuidOperacion"], unique = true)]
)
data class OperacionPendienteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uuidOperacion: String,          // el servidor lo usa para no ejecutar dos veces la misma operación
    val entidad: String,                // lead, interaccion, tarea
    val idEntidadLocal: Int,            // id local del registro afectado
    val tipoOperacion: String,          // CREAR, ACTUALIZAR, ELIMINAR
    val payload: String,                // datos a enviar (JSON)
    val fechaRegistro: Long = System.currentTimeMillis(),
    val estado: String = EstadoSincronizacion.PENDIENTE.name,
    val intentos: Int = 0,
    val mensajeError: String? = null,
    // Campos adicionales para mostrar y resolver conflictos
    val descripcion: String = "",       // texto legible: "Cambiar etapa de ... a ..."
    val codigoHttp: Int? = null,        // última respuesta del servidor (409, 503, ...)
    val respuestaServidor: String? = null // JSON del rechazo (incluye la versión actual del servidor)
)
