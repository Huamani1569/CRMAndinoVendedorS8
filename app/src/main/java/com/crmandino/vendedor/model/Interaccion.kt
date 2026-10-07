package com.crmandino.vendedor.model

// MODELO de la aplicación: lo que usan los ViewModel y las pantallas.
// (Las entidades de Room y los DTO de la API se convierten a estos modelos en data/mapper.)

data class Interaccion(
    val id: Int,
    val remoteId: Int?,
    val uuid: String,
    val leadUuid: String,
    val tipo: String,            // Llamada, Correo o Reunión
    val fecha: String,           // dd/mm/aaaa
    val nota: String,
    val version: Int,
    val estadoSync: EstadoSincronizacion,
    val mensajeError: String?
)
