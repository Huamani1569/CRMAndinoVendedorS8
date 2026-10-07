package com.crmandino.vendedor.model

// MODELO de la aplicación: lo que usan los ViewModel y las pantallas.
// (Las entidades de Room y los DTO de la API se convierten a estos modelos en data/mapper.)

// Operación de la cola local tal como se muestra en la pantalla de sincronización.
data class OperacionSync(
    val id: Int,
    val uuidOperacion: String,
    val entidad: String,
    val tipoOperacion: String,
    val descripcion: String,
    val fechaRegistro: Long,
    val estado: EstadoSincronizacion,
    val intentos: Int,
    val mensajeError: String?,
    val codigoHttp: Int?
)
