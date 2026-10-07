package com.crmandino.vendedor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crmandino.vendedor.data.repository.InteraccionRepository
import com.crmandino.vendedor.model.Interaccion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

// RF08: historial de llamadas, correos y reuniones de cada lead.
class InteraccionViewModel(private val repository: InteraccionRepository) : ViewModel() {

    val tipos = listOf("Llamada", "Correo", "Reunión")

    var error by mutableStateOf<String?>(null)
        private set

    fun interaccionesDe(leadUuid: String): Flow<List<Interaccion>> = repository.observarPorLead(leadUuid)

    // id = 0 → registrar una nueva; id > 0 → editar la existente.
    fun guardar(id: Int, leadUuid: String, tipo: String, fecha: String, nota: String, onListo: () -> Unit) {
        error = when {
            tipo !in tipos -> "Elige el tipo de interacción"
            !esFechaValida(fecha) -> "Fecha no válida (dd/mm/aaaa)"
            nota.isBlank() -> "Escribe una nota"
            else -> null
        }
        if (error != null) return

        viewModelScope.launch {
            if (id == 0) repository.registrar(leadUuid, tipo, fecha.trim(), nota.trim())
            else repository.editar(id, tipo, fecha.trim(), nota.trim())
            onListo()
        }
    }

    fun eliminar(interaccion: Interaccion) {
        viewModelScope.launch { repository.eliminar(interaccion.id) }
    }

    fun limpiarError() {
        error = null
    }
}
