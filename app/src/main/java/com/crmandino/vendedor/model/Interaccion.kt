package com.crmandino.vendedor.model

// MODELO de la aplicación: lo que usan los ViewModel y las pantallas.

data class Interaccion(
    val id: Int,
    val remoteId: Int?,
    val uuid: String,
    val leadUuid: String,
    val tipo: String,            // Llamada, WhatsApp, Correo, Reunión o Visita
    val fecha: String,           // dd/mm/aaaa
    val resultado: String?,      // CONTESTO, NO_CONTESTO, INTERESADO, NO_INTERESADO
    val nota: String,
    val version: Int,
    val estadoSync: EstadoSincronizacion,
    val mensajeError: String?
)
