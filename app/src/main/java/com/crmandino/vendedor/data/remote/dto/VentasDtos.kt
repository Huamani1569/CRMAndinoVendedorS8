package com.crmandino.vendedor.data.remote.dto

// DTO de las Semanas 12–14: catálogo, cotizaciones, ruta y visitas, clientes, recomendaciones y notificaciones.

data class ProductoDto(val id: Int, val codigo: String?, val nombre: String?, val categoria: String?, val precio: Double?)

// ---------- Cotizaciones ----------
data class ItemCotizacionRequest(val productoId: Int, val cantidad: Int)

data class CotizacionRequest(
    val uuid: String,
    val leadUuid: String,
    val descuentoPct: Double,
    val items: List<ItemCotizacionRequest>,
    val enviar: Boolean = true
)

data class ItemCotizacionDto(val productoId: Int?, val nombre: String?, val cantidad: Int?, val precioUnit: Double?, val subtotal: Double?)

data class CotizacionDto(
    val id: Int,
    val uuid: String?,
    val numero: String?,
    val leadId: Int?,
    val leadUuid: String?,
    val empresa: String?,
    val items: List<ItemCotizacionDto>?,
    val subtotal: Double?,
    val descuentoPct: Double?,
    val total: Double?,
    val estado: String?,          // BORRADOR, PEND_APROBACION, ENVIADA, ACEPTADA, RECHAZADA, VENCIDA
    val vigenciaHasta: String?,
    val diasParaVencer: Int?,
    val requiereAprobacion: Boolean?,
    val aprobadaPor: String?,
    val motivoRechazo: String?,
    val comentarioRechazo: String?,
    val enviadaEn: String?,
    val respondidaEn: String?,
    val version: Int?
)

// ---------- Visitas (ruta del día y check-in) ----------
data class VisitaDto(
    val id: Int?,
    val uuid: String?,
    val leadId: Int?,
    val leadUuid: String?,
    val empresa: String?,
    val contacto: String?,
    val telefono: String?,
    val direccion: String?,
    val puntaje: Int?,
    val etapa: String?,
    val latitudCliente: Double?,
    val longitudCliente: Double?,
    val fechaProgramada: String?,
    val checkinEn: String?,
    val distanciaM: Double?,
    val motivoDistancia: String?,
    val sugerida: Boolean?
)

data class RutaResponse(val fecha: String?, val sugerida: Boolean?, val radioMaximoM: Int?, val total: Int?, val datos: List<VisitaDto>?)

data class CheckinRequest(val latitud: Double, val longitud: Double, val precisionM: Double?, val fechaHora: String)

data class VisitaRequest(
    val uuid: String,
    val leadUuid: String,
    val fechaProgramada: String,
    val checkin: CheckinRequest?,
    val motivoDistancia: String?
)

// ---------- Clientes y recomendaciones (ML-02, ML-03, ML-04) ----------
data class CompraDto(val id: Int?, val numero: String?, val fecha: String?, val producto: String?, val cantidad: Int?, val monto: Double?)

data class FactorRiesgoDto(val factor: String?, val peso: Double?)

data class ClienteDto(
    val id: Int,
    val ruc: String?,
    val razonSocial: String?,
    val distrito: String?,
    val segmento: String?,
    val clienteDesde: String?,
    val vendedorNombre: String?,
    val puntajeValor: Int?,
    val probCancelacion: Double?,
    val factoresRiesgo: List<FactorRiesgoDto>?,
    val cltv: Double?,
    val compras: Int?,
    val comprasTotal: Double?,
    val ultimaCompra: String?,
    val ticketsPrevios: Int?,
    val csatPromedio: Double?,
    val historialCompras: List<CompraDto>?
)

data class RecomendacionDto(val productoId: Int?, val producto: String?, val precio: Double?, val confianza: Double?, val lift: Double?, val razon: String?)

// ---------- Notificaciones ----------
data class NotificacionDto(
    val id: Long,
    val tipo: String?,
    val titulo: String?,
    val cuerpo: String?,
    val refTipo: String?,
    val refId: Int?,
    val leida: Boolean?,
    val creadoEn: String?
)

data class NotificacionesResponse(val servidorTiempo: String?, val total: Int?, val noLeidas: Int?, val datos: List<NotificacionDto>?)
data class MarcarLeidasRequest(val hastaId: Long?)
data class MarcadasResponse(val marcadas: Int?)

// ---------- Historial del lead (RNF-09) ----------
data class HistorialDto(val campo: String?, val valorAnterior: String?, val valorNuevo: String?, val usuarioNombre: String?, val fecha: String?)
