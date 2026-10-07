package com.crmandino.vendedor.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

// Perfil del usuario autorizado por la API (NO se guarda la contraseña aquí).
// El token de sesión se guarda cifrado en SesionLocal (EncryptedSharedPreferences).
@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey val id: Int,      // mismo id que en el servidor
    val nombre: String,
    val correo: String,
    val rol: String,              // "cliente" = vendedor
    val esSesion: Boolean = false // true = usuario que inició sesión en este celular
)
