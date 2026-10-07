package com.crmandino.vendedor.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.crmandino.vendedor.model.UsuarioSesion
import java.security.MessageDigest
import java.util.UUID

/**
 * Sesión guardada de forma SEGURA (EncryptedSharedPreferences: cifrado con el Android Keystore).
 * Guarda: token JWT, perfil del usuario, dirección del servidor y un HASH de la clave
 * (para permitir el ingreso sin conexión a un usuario que ya inició sesión antes en este celular).
 * La contraseña original nunca se guarda.
 */
class SesionLocal(context: Context) {

    private val prefs: SharedPreferences = crearPreferencias(context)

    private fun crearPreferencias(context: Context): SharedPreferences = try {
        val llaveMaestra = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "sesion_segura",
            llaveMaestra,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // Respaldo si el Keystore del dispositivo falla (muy raro, algunos emuladores antiguos)
        context.getSharedPreferences("sesion_respaldo", Context.MODE_PRIVATE)
    }

    // Dirección de la API. 10.0.2.2 = "localhost" de la laptop vista desde el emulador.
    var baseUrl: String
        get() = prefs.getString(K_BASE_URL, null) ?: URL_POR_DEFECTO
        set(valor) = prefs.edit { putString(K_BASE_URL, normalizarUrl(valor)) }

    val token: String?
        get() = prefs.getString(K_TOKEN, null)

    val sesionActiva: Boolean
        get() = prefs.getBoolean(K_ACTIVA, false) && usuario() != null

    // Id del último usuario que usó la app en este celular (para no mezclar datos de dos usuarios)
    val ultimoUsuarioId: Int
        get() = prefs.getInt(K_ID, -1)

    fun usuario(): UsuarioSesion? {
        val id = prefs.getInt(K_ID, -1)
        val nombre = prefs.getString(K_NOMBRE, null) ?: return null
        if (id < 0) return null
        return UsuarioSesion(
            id = id,
            nombre = nombre,
            correo = prefs.getString(K_CORREO, "") ?: "",
            rol = prefs.getString(K_ROL, "") ?: ""
        )
    }

    // Se llama después de un login correcto contra la API.
    fun guardarSesion(token: String, usuario: UsuarioSesion, clave: String) {
        val sal = UUID.randomUUID().toString()
        prefs.edit {
            putString(K_TOKEN, token)
            putInt(K_ID, usuario.id)
            putString(K_NOMBRE, usuario.nombre)
            putString(K_CORREO, usuario.correo.lowercase())
            putString(K_ROL, usuario.rol)
            putString(K_SAL, sal)
            putString(K_HASH, hash(sal, usuario.correo.lowercase(), clave))
            putBoolean(K_ACTIVA, true)
        }
    }

    // Ingreso SIN conexión: solo si el correo y la clave coinciden con el último login exitoso.
    fun validarIngresoOffline(correo: String, clave: String): UsuarioSesion? {
        val usuario = usuario() ?: return null
        val sal = prefs.getString(K_SAL, null) ?: return null
        val hashGuardado = prefs.getString(K_HASH, null) ?: return null
        val correoLimpio = correo.trim().lowercase()
        if (correoLimpio != usuario.correo || hash(sal, correoLimpio, clave) != hashGuardado) return null
        prefs.edit { putBoolean(K_ACTIVA, true) }
        return usuario
    }

    // Cerrar sesión: la app vuelve al Login. Se conserva el perfil y la cola local para no perder
    // operaciones pendientes; si otro usuario inicia sesión, los datos locales se borran.
    fun cerrarSesion() {
        prefs.edit { putBoolean(K_ACTIVA, false) }
    }

    private fun hash(sal: String, correo: String, clave: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest("$sal|$correo|$clave".toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        const val URL_POR_DEFECTO = "http://10.0.2.2:3000/"

        private const val K_BASE_URL = "base_url"
        private const val K_TOKEN = "token"
        private const val K_ID = "usuario_id"
        private const val K_NOMBRE = "usuario_nombre"
        private const val K_CORREO = "usuario_correo"
        private const val K_ROL = "usuario_rol"
        private const val K_SAL = "clave_sal"
        private const val K_HASH = "clave_hash"
        private const val K_ACTIVA = "sesion_activa"

        fun normalizarUrl(url: String): String {
            var limpia = url.trim()
            if (!limpia.startsWith("http://") && !limpia.startsWith("https://")) limpia = "http://$limpia"
            if (!limpia.endsWith("/")) limpia = "$limpia/"
            return limpia
        }
    }
}
