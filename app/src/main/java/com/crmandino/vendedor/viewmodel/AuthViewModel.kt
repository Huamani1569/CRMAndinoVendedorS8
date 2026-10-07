package com.crmandino.vendedor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crmandino.vendedor.data.local.SesionLocal
import com.crmandino.vendedor.data.repository.AuthRepository
import com.crmandino.vendedor.data.repository.ResultadoLogin
import com.crmandino.vendedor.data.repository.SyncRepository
import kotlinx.coroutines.launch

// Login contra la API REST (RFA03–RFA05) con sesión segura y acceso offline para usuarios ya autenticados.
class AuthViewModel(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository
) : ViewModel() {

    // Si quedó una sesión abierta, la app entra directo (también sin conexión).
    var usuarioActual by mutableStateOf(authRepository.sesionGuardada())
        private set
    var cargando by mutableStateOf(false)
        private set
    var textoCarga by mutableStateOf("Validando...")
        private set

    var errorCorreo by mutableStateOf<String?>(null)
        private set
    var errorClave by mutableStateOf<String?>(null)
        private set
    var mensajeError by mutableStateOf<String?>(null)
        private set

    // Aviso que se muestra en el inicio si se ingresó sin conexión
    var avisoSinConexion by mutableStateOf<String?>(null)
        private set

    var servidor by mutableStateOf(authRepository.servidor)
        private set

    private val formatoCorreo = Regex("^[\\w.+-]+@[\\w-]+\\.[\\w.]+$")

    fun iniciarSesion(correo: String, clave: String) {
        limpiarErrores()
        val correoLimpio = correo.trim().lowercase()
        errorCorreo = when {
            correoLimpio.isBlank() -> "Ingresa tu correo"
            !formatoCorreo.matches(correoLimpio) -> "Formato de correo no válido"
            else -> null
        }
        errorClave = if (clave.isBlank()) "Ingresa tu contraseña" else null
        if (errorCorreo != null || errorClave != null) return

        viewModelScope.launch {
            cargando = true
            textoCarga = "Validando..."
            when (val resultado = authRepository.login(correoLimpio, clave)) {
                is ResultadoLogin.Exito -> {
                    if (resultado.sinConexion) {
                        avisoSinConexion = "Ingresaste SIN CONEXIÓN con tu sesión guardada: verás los datos locales y tus cambios quedarán pendientes."
                    } else {
                        // RF11: primera descarga de datos desde la API a Room
                        textoCarga = "Descargando datos..."
                        syncRepository.sincronizar()
                        avisoSinConexion = null
                    }
                    usuarioActual = resultado.usuario
                }
                is ResultadoLogin.Error -> mensajeError = resultado.mensaje
            }
            cargando = false
        }
    }

    fun guardarServidor(url: String) {
        if (url.isBlank()) return
        authRepository.servidor = url
        servidor = authRepository.servidor
    }

    fun restablecerServidor() = guardarServidor(SesionLocal.URL_POR_DEFECTO)

    fun cerrarSesion() {
        authRepository.cerrarSesion()
        usuarioActual = null
        avisoSinConexion = null
        limpiarErrores()
    }

    fun limpiarErrores() {
        errorCorreo = null
        errorClave = null
        mensajeError = null
    }

    fun descartarAviso() {
        avisoSinConexion = null
    }
}
