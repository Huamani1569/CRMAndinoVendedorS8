package com.crmandino.vendedor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crmandino.vendedor.data.repository.SyncRepository
import com.crmandino.vendedor.model.OperacionSync
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Estado de la sincronización (RF13, RF14, RF15): conexión, pendientes, errores, última
// sincronización, botón manual y resolución de operaciones rechazadas.
class SyncViewModel(
    private val syncRepository: SyncRepository,
    val conectado: StateFlow<Boolean>,
    private val programarEnSegundoPlano: () -> Unit
) : ViewModel() {

    val pendientes: Flow<Int> = syncRepository.pendientes
    val errores: Flow<Int> = syncRepository.errores
    val ultimaSincronizacion: Flow<Long?> = syncRepository.ultimaSincronizacion
    val operaciones: Flow<List<OperacionSync>> = syncRepository.operaciones

    var sincronizando by mutableStateOf(false)
        private set
    var mensaje by mutableStateOf<String?>(null)
        private set

    // Botón "Sincronizar ahora"
    fun sincronizarAhora() {
        if (sincronizando) return
        if (!conectado.value) {
            programarEnSegundoPlano()
            mensaje = "Sin conexión: tus cambios quedan PENDIENTES en el celular y se enviarán solos al volver la conexión."
            return
        }
        viewModelScope.launch {
            sincronizando = true
            mensaje = syncRepository.sincronizar().mensaje
            sincronizando = false
        }
    }

    fun descartar(operacion: OperacionSync) {
        viewModelScope.launch {
            syncRepository.descartarCambio(operacion.id)
            mensaje = "Cambio descartado: se usará la versión del servidor."
            sincronizarAhora()
        }
    }

    fun reintentar(operacion: OperacionSync) {
        viewModelScope.launch {
            syncRepository.reintentar(operacion.id)
            sincronizarAhora()
        }
    }

    // Evidencia de "ausencia de duplicados"
    fun probarDuplicado(operacion: OperacionSync) {
        if (!conectado.value) {
            mensaje = "Necesitas conexión para la prueba de duplicados."
            return
        }
        viewModelScope.launch {
            sincronizando = true
            mensaje = syncRepository.reenviarComoPrueba(operacion.id)
            sincronizando = false
        }
    }

    fun limpiarMensaje() {
        mensaje = null
    }
}
