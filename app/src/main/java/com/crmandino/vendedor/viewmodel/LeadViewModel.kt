package com.crmandino.vendedor.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crmandino.vendedor.data.repository.DatosLead
import com.crmandino.vendedor.data.repository.LeadRepository
import com.crmandino.vendedor.model.EtapaVenta
import com.crmandino.vendedor.model.Lead
import com.crmandino.vendedor.model.ResumenEtapa
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

// Orden de la cartera (RF-V02): por puntaje (probabilidad de cierre), por días sin contacto o por valor.
enum class OrdenCartera(val etiqueta: String) { PUNTAJE("Puntaje"), SIN_CONTACTO("Días sin contacto"), VALOR("Valor") }

// Errores del formulario del lead, por campo.
data class ErroresLead(val empresa: String? = null, val valor: String? = null, val telefono: String? = null, val ruc: String? = null, val correo: String? = null) {
    val hay get() = listOf(empresa, valor, telefono, ruc, correo).any { it != null }
}

// Leads del vendedor: cartera (Room) ordenada por puntaje, crear/editar y cambio de etapa.
// El ViewModel valida y llama al Repository; nunca toca DAO ni Retrofit directamente.
class LeadViewModel(private val leadRepository: LeadRepository) : ViewModel() {

    val leads: Flow<List<Lead>> = leadRepository.observarLeads()

    // Valor en embudo: suma del valor potencial de los leads abiertos.
    val valorEnEmbudo: Flow<Double> = leads.map { lista ->
        lista.filter { it.etapaVenta.abierta }.sumOf { it.valorPotencial }
    }

    // Valor ESPERADO: Σ valor × probabilidad de cierre (ML-01)
    val valorEsperado: Flow<Double> = leads.map { lista ->
        lista.filter { it.etapaVenta.abierta }.sumOf { it.valorPotencial * (it.puntaje ?: 30) / 100.0 }
    }

    // Datos del embudo dibujado con Canvas (cantidad y valor por etapa)
    val resumenPorEtapa: Flow<List<ResumenEtapa>> = leads.map { lista ->
        EtapaVenta.entries.filter { it != EtapaVenta.PERDIDO }.map { etapa ->
            val deLaEtapa = lista.filter { it.etapaVenta == etapa }
            ResumenEtapa(etapa.name, deLaEtapa.size, deLaEtapa.sumOf { it.valorPotencial })
        }
    }

    var erroresFormulario by mutableStateOf(ErroresLead())
        private set
    var errorEtapa by mutableStateOf<String?>(null)
        private set

    // Cartera filtrada y ordenada (null = todas las etapas)
    fun cartera(etapa: String?, orden: OrdenCartera, texto: String): Flow<List<Lead>> =
        leadRepository.observarPorEtapa(etapa).map { lista ->
            val filtro = texto.trim().lowercase()
            val filtrada = if (filtro.isEmpty()) lista else lista.filter {
                it.empresa.lowercase().contains(filtro) || it.contacto.lowercase().contains(filtro) || (it.ruc ?: "").startsWith(filtro)
            }
            when (orden) {
                OrdenCartera.PUNTAJE -> filtrada.sortedWith(compareByDescending<Lead> { it.etapaVenta.abierta }.thenByDescending { it.puntaje ?: -1 })
                OrdenCartera.SIN_CONTACTO -> filtrada.sortedWith(compareByDescending<Lead> { it.etapaVenta.abierta }.thenByDescending { it.diasSinContacto ?: -1 })
                OrdenCartera.VALOR -> filtrada.sortedByDescending { it.valorPotencial }
            }
        }

    fun leadPorId(id: Int): Flow<Lead?> = leadRepository.observarLead(id)

    // RF10: no se guarda un lead sin empresa. id = 0 → nuevo; id > 0 → editar.
    fun guardarLead(
        id: Int, empresa: String, contacto: String, telefono: String, correo: String, ruc: String,
        direccion: String, valorTexto: String, latitud: Double?, longitud: Double?, onGuardado: () -> Unit
    ) {
        val valor = valorTexto.trim().replace(",", "").ifBlank { "0" }.toDoubleOrNull()
        val tel = telefono.filter { it.isDigit() }
        val rucLimpio = ruc.filter { it.isDigit() }
        val e = ErroresLead(
            empresa = if (empresa.isBlank()) "La empresa es obligatoria" else null,
            valor = when {
                valor == null -> "Ingresa un valor válido"
                valor < 0 -> "No puede ser negativo"
                else -> null
            },
            telefono = if (tel.isNotEmpty() && !Regex("^9\\d{8}$").matches(tel)) "Celular de 9 dígitos que empiece en 9" else null,
            ruc = if (rucLimpio.isNotEmpty() && rucLimpio.length != 11) "El RUC tiene 11 dígitos" else null,
            correo = if (correo.isNotBlank() && !Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$").matches(correo.trim())) "Correo no válido" else null
        )
        erroresFormulario = e
        if (e.hay || valor == null) return
        val datos = DatosLead(
            empresa.trim(), contacto.trim(), tel.ifBlank { null }, correo.trim().lowercase().ifBlank { null },
            rucLimpio.ifBlank { null }, direccion.trim().ifBlank { null }, latitud, longitud, valor
        )
        viewModelScope.launch {
            if (id == 0) leadRepository.crearLead(datos) else leadRepository.actualizarLead(id, datos)
            onGuardado()
        }
    }

    // Cambia la etapa (con motivo si se pierde) y opcionalmente crea un recordatorio.
    fun cambiarEtapa(lead: Lead, nueva: EtapaVenta, motivo: String, recordatorio: String, fechaLimite: String, onListo: () -> Unit) {
        errorEtapa = when {
            nueva == EtapaVenta.PERDIDO && motivo.isBlank() -> "Indica por qué se perdió"
            nueva == EtapaVenta.GANADO && lead.ruc.isNullOrBlank() -> "Para marcarlo GANADO registra primero el RUC (edita el lead)"
            recordatorio.isNotBlank() && !esFechaValida(fechaLimite) -> "Fecha límite no válida (dd/mm/aaaa)"
            else -> null
        }
        if (errorEtapa != null) return
        viewModelScope.launch {
            leadRepository.cambiarEtapa(lead.id, nueva, motivo.trim().ifBlank { null }, recordatorio.trim().ifBlank { null }, fechaLimite.trim())
            onListo()
        }
    }

    fun limpiarErrores() {
        erroresFormulario = ErroresLead()
        errorEtapa = null
    }
}

// Valida que la fecha exista y tenga el formato dd/mm/aaaa (lo usan varios ViewModel).
fun esFechaValida(texto: String): Boolean {
    if (!Regex("\\d{2}/\\d{2}/\\d{4}").matches(texto.trim())) return false
    val formato = SimpleDateFormat("dd/MM/yyyy", Locale.US).apply { isLenient = false }
    return runCatching { formato.parse(texto.trim()) }.isSuccess
}
