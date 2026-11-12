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

// Resultados de un intento de contacto (RF-V05) y su texto en pantalla.
val RESULTADOS_CONTACTO = linkedMapOf(
    "CONTESTO" to "Contestó",
    "NO_CONTESTO" to "No contestó",
    "INTERESADO" to "Interesado",
    "NO_INTERESADO" to "No interesado"
)

// RF-V05/V06: historial de llamadas, WhatsApp, correos, reuniones y visitas de cada lead.
class InteraccionViewModel(private val repository: InteraccionRepository) : ViewModel() {

    val tipos = listOf("Llamada", "WhatsApp", "Correo", "Reunión", "Visita")

    var error by mutableStateOf<String?>(null)
        private set

    fun interaccionesDe(leadUuid: String): Flow<List<Interaccion>> = repository.observarPorLead(leadUuid)

    // id = 0 → registrar una nueva; id > 0 → editar la existente. La nota solo es obligatoria en reuniones.
    fun guardar(id: Int, leadUuid: String, tipo: String, fecha: String, resultado: String?, nota: String, onListo: () -> Unit) {
        error = when {
            tipo !in tipos -> "Elige el tipo de interacción"
            !esFechaValida(fecha) -> "Fecha no válida (dd/mm/aaaa)"
            tipo == "Reunión" && nota.isBlank() -> "En una reunión la nota es obligatoria"
            else -> null
        }
        if (error != null) return

        viewModelScope.launch {
            if (id == 0) repository.registrar(leadUuid, tipo, fecha.trim(), resultado, nota.trim())
            else repository.editar(id, tipo, fecha.trim(), resultado, nota.trim())
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
