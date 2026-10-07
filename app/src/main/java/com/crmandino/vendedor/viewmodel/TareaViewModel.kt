package com.crmandino.vendedor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crmandino.vendedor.data.repository.TareaRepository
import com.crmandino.vendedor.model.Tarea
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

// Recordatorios de seguimiento (tareas con fecha límite).
class TareaViewModel(private val repository: TareaRepository) : ViewModel() {

    val pendientes: Flow<List<Tarea>> = repository.observarPendientes()

    fun tareasDe(leadUuid: String): Flow<List<Tarea>> = repository.observarPorLead(leadUuid)

    // Marca o desmarca la tarea como completada.
    fun cambiarEstado(tarea: Tarea) {
        viewModelScope.launch { repository.cambiarEstado(tarea.id) }
    }

    fun eliminar(tarea: Tarea) {
        viewModelScope.launch { repository.eliminar(tarea.id) }
    }
}
