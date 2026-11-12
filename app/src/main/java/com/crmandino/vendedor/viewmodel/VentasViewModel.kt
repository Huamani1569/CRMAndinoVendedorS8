package com.crmandino.vendedor.viewmodel

import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crmandino.vendedor.data.remote.dto.ClienteDto
import com.crmandino.vendedor.data.remote.dto.CotizacionDto
import com.crmandino.vendedor.data.remote.dto.HistorialDto
import com.crmandino.vendedor.data.remote.dto.NotificacionDto
import com.crmandino.vendedor.data.remote.dto.ProductoDto
import com.crmandino.vendedor.data.remote.dto.RecomendacionDto
import com.crmandino.vendedor.data.remote.dto.RutaResponse
import com.crmandino.vendedor.data.repository.ItemCarrito
import com.crmandino.vendedor.data.repository.VentasRepository
import com.crmandino.vendedor.model.Lead
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

// Resultado de calcular la distancia antes del check-in.
data class PreCheckin(val lead: Lead, val ubicacion: Location, val distancia: Double?) {
    val fueraDeRango: Boolean get() = (distancia ?: 0.0) > VentasRepository.RADIO_MAXIMO_M
}

// Semanas 12–14 en la app Vendedor: cotizaciones, ruta y check-in, ficha del cliente y notificaciones.
class VentasViewModel(private val repo: VentasRepository, val conectado: StateFlow<Boolean>) : ViewModel() {

    private fun <T> Flow<T>.estado(inicial: T): StateFlow<T> = stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), inicial)

    val productos: StateFlow<List<ProductoDto>> = repo.productos.estado(emptyList())
    val cotizaciones: StateFlow<List<CotizacionDto>> = repo.cotizaciones.estado(emptyList())
    val ruta: StateFlow<RutaResponse?> = repo.ruta.estado(null)
    val notificaciones: StateFlow<List<NotificacionDto>> = repo.notificaciones.estado(emptyList())
    val noLeidas: StateFlow<Int> = repo.notificaciones.map { l -> l.count { it.leida != true } }.estado(0)

    fun cliente(id: Int): Flow<ClienteDto?> = repo.cliente(id)
    fun recomendaciones(id: Int): Flow<List<RecomendacionDto>> = repo.recomendaciones(id)
    fun historial(leadId: Int): Flow<List<HistorialDto>> = repo.historial(leadId)

    var mensaje by mutableStateOf<String?>(null)
        private set
    var mensajeEsError by mutableStateOf(false)
        private set
    var cargando by mutableStateOf(false)
        private set

    private var sondeo: Job? = null

    fun avisar(texto: String, error: Boolean = false) {
        mensaje = texto
        mensajeEsError = error
        viewModelScope.launch { delay(5_000); if (mensaje == texto) mensaje = null }
    }

    fun cerrarMensaje() { mensaje = null }

    // Mientras la app está abierta revisa cada 30 s si hay notificaciones nuevas
    fun iniciarSondeo() {
        if (sondeo?.isActive == true) return
        sondeo = viewModelScope.launch {
            while (isActive) {
                delay(30_000)
                if (conectado.value) {
                    repo.refrescarNotificaciones()
                    repo.refrescarCotizaciones()
                }
            }
        }
    }

    fun refrescarRuta() = viewModelScope.launch {
        cargando = true
        repo.refrescarRuta()?.let { avisar(if (conectado.value) it else "Sin conexión: se muestra la última ruta descargada.", true) }
        cargando = false
    }

    fun refrescarCotizaciones() = viewModelScope.launch { repo.refrescarCotizaciones(); repo.refrescarProductos() }
    fun refrescarCliente(id: Int) = viewModelScope.launch { repo.refrescarCliente(id) }
    fun refrescarHistorial(leadId: Int) = viewModelScope.launch { repo.refrescarHistorial(leadId) }
    fun marcarLeidas() = viewModelScope.launch { repo.marcarLeidas() }

    // ---------- Cotización (RF-V09, RF-V10) ----------
    fun crearCotizacion(lead: Lead, carrito: List<ItemCarrito>, descuentoTexto: String, onListo: () -> Unit): String? {
        val descuento = descuentoTexto.replace(",", ".").ifBlank { "0" }.toDoubleOrNull()
        val error = when {
            !lead.etapaVenta.abierta -> "Este lead está cerrado (${lead.etapaVenta.etiqueta})"
            lead.etapa !in listOf("NUEVO", "CONTACTADO", "CALIFICADO") -> "Solo se cotiza un lead en etapa Nuevo, Contactado o Calificado"
            carrito.none { it.cantidad > 0 } -> "Agrega al menos un producto"
            descuento == null || descuento < 0 || descuento > 50 -> "El descuento debe estar entre 0 % y 50 %"
            else -> null
        }
        if (error != null || descuento == null) return error
        viewModelScope.launch {
            repo.crearCotizacion(lead, carrito, descuento)
            avisar(
                if (descuento > VentasRepository.DESCUENTO_MAXIMO) "Cotización registrada: el descuento de $descuento % pasa al supervisor para su aprobación."
                else "Cotización enviada al cliente${if (conectado.value) "" else " (se enviará al recuperar la conexión)"}."
            )
            onListo()
        }
        return null
    }

    // ---------- Check-in (RF-V12, RF-V13) ----------
    fun calcularCheckin(lead: Lead, ubicacion: Location): PreCheckin {
        val lat = lead.latitud
        val lon = lead.longitud
        val distancia = if (lat != null && lon != null) VentasRepository.distanciaMetros(ubicacion.latitude, ubicacion.longitude, lat, lon) else null
        return PreCheckin(lead, ubicacion, distancia)
    }

    fun confirmarCheckin(p: PreCheckin, motivo: String, onListo: () -> Unit): String? {
        if (p.fueraDeRango && motivo.isBlank()) return "Estás a ${p.distancia?.toInt()} m del cliente: indica el motivo para registrar el check-in."
        viewModelScope.launch {
            repo.registrarCheckin(
                p.lead, p.ubicacion.latitude, p.ubicacion.longitude,
                if (p.ubicacion.hasAccuracy()) p.ubicacion.accuracy.toDouble() else null,
                motivo.trim().ifBlank { null }, p.distancia
            )
            avisar("Check-in registrado en ${p.lead.empresa}" + (p.distancia?.let { " (a ${it.toInt()} m)" } ?: "") + ".")
            onListo()
        }
        return null
    }
}
