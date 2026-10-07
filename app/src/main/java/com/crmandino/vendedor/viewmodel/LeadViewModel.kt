package com.crmandino.vendedor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crmandino.vendedor.data.repository.LeadRepository
import com.crmandino.vendedor.model.EtapaVenta
import com.crmandino.vendedor.model.Lead
import com.crmandino.vendedor.model.ResumenEtapa
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

// Leads del vendedor: listado (Room), filtro por etapa, crear/editar y cambio de etapa.
// El ViewModel valida y llama al Repository; nunca toca DAO ni Retrofit directamente.
class LeadViewModel(private val leadRepository: LeadRepository) : ViewModel() {

    // RF06: los leads se leen desde Room
    val leads: Flow<List<Lead>> = leadRepository.observarLeads()

    // Valor en embudo: suma del valor potencial de los leads que aún no están cerrados.
    val valorEnEmbudo: Flow<Double> = leads.map { lista ->
        lista.filter { it.etapa != EtapaVenta.CERRADO.etiqueta }.sumOf { it.valorPotencial }
    }

    // Datos del embudo dibujado con Canvas (cantidad y valor por etapa)
    val resumenPorEtapa: Flow<List<ResumenEtapa>> = leads.map { lista ->
        EtapaVenta.etiquetas.map { etapa ->
            val deLaEtapa = lista.filter { it.etapa == etapa }
            ResumenEtapa(etapa, deLaEtapa.size, deLaEtapa.sumOf { it.valorPotencial })
        }
    }

    var errorFormulario by mutableStateOf<String?>(null)
        private set
    var errorEtapa by mutableStateOf<String?>(null)
        private set

    // RF07: filtrar por etapa (null = todas)
    fun leadsPorEtapa(etapa: String?): Flow<List<Lead>> = leadRepository.observarPorEtapa(etapa)

    fun leadPorId(id: Int): Flow<Lead?> = leadRepository.observarLead(id)

    // RF10: no se guarda un lead sin empresa. id = 0 → nuevo; id > 0 → editar.
    fun guardarLead(
        id: Int, empresa: String, contacto: String, valorTexto: String,
        etapa: String, onGuardado: () -> Unit
    ) {
        val valor = valorTexto.trim().replace(",", "").toDoubleOrNull()
        errorFormulario = when {
            empresa.isBlank() -> "La empresa es obligatoria"
            valor == null -> "Ingresa un valor potencial válido"
            valor < 0 -> "El valor potencial no puede ser negativo"
            else -> null
        }
        if (errorFormulario != null || valor == null) return

        viewModelScope.launch {
            if (id == 0) {
                leadRepository.crearLead(empresa.trim(), contacto.trim(), valor, etapa)
            } else {
                leadRepository.actualizarLead(id, empresa.trim(), contacto.trim(), valor, etapa)
            }
            onGuardado()
        }
    }

    // RF09: cambia la etapa y crea un recordatorio con fecha límite.
    fun cambiarEtapa(lead: Lead, nuevaEtapa: String, recordatorio: String, fechaLimite: String, onListo: () -> Unit) {
        errorEtapa = when {
            recordatorio.isBlank() -> "Escribe el recordatorio"
            !esFechaValida(fechaLimite) -> "Fecha límite no válida (dd/mm/aaaa)"
            else -> null
        }
        if (errorEtapa != null) return

        viewModelScope.launch {
            leadRepository.cambiarEtapaConRecordatorio(lead.id, nuevaEtapa, recordatorio.trim(), fechaLimite.trim())
            onListo()
        }
    }

    fun limpiarErrores() {
        errorFormulario = null
        errorEtapa = null
    }
}

// Valida que la fecha exista y tenga el formato dd/mm/aaaa (lo usan varios ViewModel).
fun esFechaValida(texto: String): Boolean {
    if (!Regex("\\d{2}/\\d{2}/\\d{4}").matches(texto.trim())) return false
    val formato = SimpleDateFormat("dd/MM/yyyy", Locale.US).apply { isLenient = false }
    return runCatching { formato.parse(texto.trim()) }.isSuccess
}
