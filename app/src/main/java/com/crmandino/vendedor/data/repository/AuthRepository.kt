package com.crmandino.vendedor.data.repository

import androidx.room.withTransaction
import com.crmandino.vendedor.data.local.AppDatabase
import com.crmandino.vendedor.data.local.SesionLocal
import com.crmandino.vendedor.data.local.entities.UsuarioEntity
import com.crmandino.vendedor.data.remote.api.ApiService
import com.crmandino.vendedor.data.remote.dto.ErrorResponse
import com.crmandino.vendedor.data.remote.dto.LoginRequest
import com.crmandino.vendedor.model.RolUsuario
import com.crmandino.vendedor.model.UsuarioSesion
import com.google.gson.Gson
import java.io.IOException

sealed class ResultadoLogin {
    data class Exito(val usuario: UsuarioSesion, val sinConexion: Boolean) : ResultadoLogin()
    data class Error(val mensaje: String) : ResultadoLogin()
}

// Autenticación contra la API REST (POST /api/auth/login) y sesión local segura.
class AuthRepository(
    private val db: AppDatabase,
    private val api: ApiService,
    private val sesion: SesionLocal
) {

    // Sesión que quedó abierta la última vez (permite abrir la app sin conexión)
    fun sesionGuardada(): UsuarioSesion? = if (sesion.sesionActiva) sesion.usuario() else null

    var servidor: String
        get() = sesion.baseUrl
        set(valor) {
            sesion.baseUrl = valor
        }

    suspend fun login(correo: String, clave: String): ResultadoLogin {
        val correoLimpio = correo.trim().lowercase()
        return try {
            val respuesta = api.login(LoginRequest(correoLimpio, clave, RolUsuario.VALOR))
            val cuerpo = respuesta.body()
            when {
                respuesta.isSuccessful && cuerpo != null -> {
                    val usuario = UsuarioSesion(cuerpo.usuario.id, cuerpo.usuario.nombre, cuerpo.usuario.correo, cuerpo.usuario.rol)
                    // Si entra otro usuario en este celular, se borran los datos locales del anterior.
                    if (sesion.ultimoUsuarioId != usuario.id) limpiarDatosLocales()
                    sesion.guardarSesion(cuerpo.token, usuario, clave)
                    db.usuarioDao().guardar(UsuarioEntity(usuario.id, usuario.nombre, usuario.correo, usuario.rol, esSesion = true))
                    ResultadoLogin.Exito(usuario, sinConexion = false)
                }
                respuesta.code() >= 500 -> intentarSinConexion(correoLimpio, clave, "El servidor no está disponible (${respuesta.code()}).")
                else -> ResultadoLogin.Error(mensajeDeError(respuesta.errorBody()?.string()) ?: "No se pudo iniciar sesión (${respuesta.code()}).")
            }
        } catch (e: IOException) {
            intentarSinConexion(correoLimpio, clave, "Sin conexión con el servidor.")
        }
    }

    // Acceso offline: solo para el usuario que ya se autenticó antes en este celular.
    private fun intentarSinConexion(correo: String, clave: String, motivo: String): ResultadoLogin =
        sesion.validarIngresoOffline(correo, clave)?.let { ResultadoLogin.Exito(it, sinConexion = true) }
            ?: ResultadoLogin.Error("$motivo Sin conexión solo puede ingresar una cuenta que ya inició sesión antes en este celular.")

    fun cerrarSesion() = sesion.cerrarSesion()

    private suspend fun limpiarDatosLocales() {
        db.withTransaction {
            db.operacionPendienteDao().borrarTodo()
            db.interaccionDao().borrarTodo()
            db.tareaDao().borrarTodo()
            db.leadDao().borrarTodo()
            db.syncMetadataDao().borrarTodo()
            db.usuarioDao().borrarTodo()
        }
    }

    private fun mensajeDeError(json: String?): String? =
        runCatching { Gson().fromJson(json, ErrorResponse::class.java)?.mensaje }.getOrNull()
}
