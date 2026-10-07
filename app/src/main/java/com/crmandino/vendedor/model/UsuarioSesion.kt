package com.crmandino.vendedor.model

// MODELO de la aplicación: lo que usan los ViewModel y las pantallas.
// (Las entidades de Room y los DTO de la API se convierten a estos modelos en data/mapper.)

data class UsuarioSesion(
    val id: Int,          // id del usuario en el servidor
    val nombre: String,
    val correo: String,
    val rol: String
)
