package com.crmandino.vendedor.model

// MODELO de la aplicación: lo que usan los ViewModel y las pantallas.
// (Las entidades de Room y los DTO de la API se convierten a estos modelos en data/mapper.)

data class Tarea(
    val id: Int,
    val remoteId: Int?,
    val uuid: String,
    val leadUuid: String,
    val leadEmpresa: String,
    val descripcion: String,
    val fechaLimite: String,     // dd/mm/aaaa
    val completada: Boolean,
    val vendedorNombre: String,
    val version: Int,
    val estadoSync: EstadoSincronizacion,
    val mensajeError: String?
)
