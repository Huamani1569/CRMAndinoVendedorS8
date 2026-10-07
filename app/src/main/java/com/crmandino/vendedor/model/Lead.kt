package com.crmandino.vendedor.model

// MODELO de la aplicación: lo que usan los ViewModel y las pantallas.
// (Las entidades de Room y los DTO de la API se convierten a estos modelos en data/mapper.)

data class Lead(
    val id: Int,                 // identificador LOCAL (Room)
    val remoteId: Int?,          // identificador REMOTO (null si aún no llega al servidor)
    val uuid: String,            // identificador global, se genera en el celular
    val empresa: String,
    val contacto: String,
    val valorPotencial: Double,
    val etapa: String,           // estado del NEGOCIO: Prospecto, Contactado, ...
    val vendedorId: Int,
    val vendedorNombre: String,
    val version: Int,            // última versión confirmada por el servidor
    val estadoSync: EstadoSincronizacion,
    val mensajeError: String?
)

// Resumen de una etapa del embudo (para el gráfico).
data class ResumenEtapa(val etapa: String, val cantidad: Int, val valor: Double)
