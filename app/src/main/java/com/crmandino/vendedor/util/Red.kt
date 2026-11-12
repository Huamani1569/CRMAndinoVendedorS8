package com.crmandino.vendedor.util

import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Response
import java.io.IOException

// Resultado de una llamada a la API: éxito con datos o error con un mensaje claro para el usuario.
sealed class Resultado<out T> {
    data class Ok<T>(val dato: T) : Resultado<T>()
    data class Error(val mensaje: String, val codigo: Int? = null, val cuerpo: String? = null) : Resultado<Nothing>() {
        val sinConexion: Boolean get() = codigo == null
    }
}

// Cuerpo de error que devuelve la API: { error, mensaje, actual }
data class ErrorApi(val error: String?, val mensaje: String?, val actual: JsonObject?)

private val gsonRed = Gson()

fun mensajeDeError(json: String?): String? =
    runCatching { gsonRed.fromJson(json, ErrorApi::class.java)?.mensaje }.getOrNull()

fun codigoDeError(json: String?): String? =
    runCatching { gsonRed.fromJson(json, ErrorApi::class.java)?.error }.getOrNull()

// Ejecuta la petición y convierte las excepciones de red en un Resultado.Error "sin conexión".
suspend fun <T> llamarApi(bloque: suspend () -> Response<T>): Resultado<T> = try {
    val r = bloque()
    val cuerpo = r.body()
    if (r.isSuccessful && cuerpo != null) {
        Resultado.Ok(cuerpo)
    } else {
        val error = r.errorBody()?.string()
        val mensaje = when {
            r.code() == 401 -> "Tu sesión expiró. Vuelve a iniciar sesión."
            r.code() >= 500 -> mensajeDeError(error) ?: "El servidor no está disponible (${r.code()})."
            else -> mensajeDeError(error) ?: "No se pudo completar la operación (${r.code()})."
        }
        Resultado.Error(mensaje, r.code(), error)
    }
} catch (e: IOException) {
    Resultado.Error("Sin conexión con el servidor. Revisa tu internet o la dirección del servidor.")
} catch (e: com.google.gson.JsonParseException) {
    Resultado.Error("La respuesta del servidor no tiene el formato esperado.", -1)
}
