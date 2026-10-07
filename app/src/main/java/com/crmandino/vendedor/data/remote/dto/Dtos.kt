package com.crmandino.vendedor.data.remote.dto

import com.google.gson.JsonObject

// DTO (Data Transfer Object): la forma EXACTA del JSON que envía y recibe la API REST.

// ---------- Autenticación ----------
data class LoginRequest(val correo: String, val clave: String, val rolEsperado: String)

data class UsuarioDto(val id: Int, val nombre: String, val correo: String, val rol: String)

data class LoginResponse(val token: String, val usuario: UsuarioDto)

// ---------- Respuestas genéricas ----------
// GET de un listado: { servidorTiempo, total, datos: [...] }
data class ListaResponse<T>(val servidorTiempo: String?, val total: Int?, val datos: List<T>?)

// Escrituras: { dato: {...}, duplicada: true|false }
data class DatoResponse<T>(val dato: T?, val duplicada: Boolean?)

// Error: { error: "CONFLICTO_VERSION", mensaje: "...", actual: {...} }
data class ErrorResponse(val error: String?, val mensaje: String?, val actual: JsonObject?)

data class SaludResponse(val ok: Boolean?, val baseDatos: String?)
