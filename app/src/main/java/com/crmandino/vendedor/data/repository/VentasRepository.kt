package com.crmandino.vendedor.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.crmandino.vendedor.data.local.AppDatabase
import com.crmandino.vendedor.data.local.SesionLocal
import com.crmandino.vendedor.data.local.entities.CacheEntity
import com.crmandino.vendedor.data.remote.api.ApiService
import com.crmandino.vendedor.data.remote.dto.CheckinRequest
import com.crmandino.vendedor.data.remote.dto.ClienteDto
import com.crmandino.vendedor.data.remote.dto.CotizacionDto
import com.crmandino.vendedor.data.remote.dto.CotizacionRequest
import com.crmandino.vendedor.data.remote.dto.HistorialDto
import com.crmandino.vendedor.data.remote.dto.ItemCotizacionRequest
import com.crmandino.vendedor.data.remote.dto.MarcarLeidasRequest
import com.crmandino.vendedor.data.remote.dto.NotificacionDto
import com.crmandino.vendedor.data.remote.dto.ProductoDto
import com.crmandino.vendedor.data.remote.dto.RecomendacionDto
import com.crmandino.vendedor.data.remote.dto.RutaResponse
import com.crmandino.vendedor.data.remote.dto.VisitaRequest
import com.crmandino.vendedor.model.EntidadSync
import com.crmandino.vendedor.model.EtapaVenta
import com.crmandino.vendedor.model.Lead
import com.crmandino.vendedor.model.TipoOperacion
import com.crmandino.vendedor.util.Formato
import com.crmandino.vendedor.util.Notificador
import com.crmandino.vendedor.util.Resultado
import com.crmandino.vendedor.util.llamarApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

// Ítem del carrito de la cotización.
data class ItemCarrito(val producto: ProductoDto, val cantidad: Int) {
    val subtotal: Double get() = (producto.precio ?: 0.0) * cantidad
}

/**
 * Semanas 12–14 en la app Vendedor: catálogo, cotizaciones, ruta del día con check-in GPS,
 * ficha del cliente con recomendaciones (ML-04) y notificaciones.
 *  - LECTURA: caché JSON en Room (se ve sin conexión) que se actualiza al sincronizar.
 *  - ESCRITURA: la cotización y el check-in van a la MISMA cola de operaciones del parcial
 *    (con X-Operacion-UUID), así funcionan sin internet y nunca se duplican.
 */
class VentasRepository(
    private val context: Context,
    private val db: AppDatabase,
    private val api: ApiService,
    private val sesion: SesionLocal,
    private val cola: ColaOperaciones,
    private val programarSync: () -> Unit
) {
    private val gson = Gson()
    private val cache = db.cacheDao()

    private inline fun <reified T> observar(clave: String): Flow<T?> {
        val tipo = object : TypeToken<T>() {}.type
        return cache.observar(clave).map { fila -> fila?.let { runCatching { gson.fromJson<T>(it.json, tipo) }.getOrNull() } }
    }

    private suspend fun guardar(clave: String, dato: Any) = cache.guardar(CacheEntity(clave, gson.toJson(dato)))

    val productos: Flow<List<ProductoDto>> = observar<List<ProductoDto>>(C_PRODUCTOS).map { it.orEmpty() }
    val cotizaciones: Flow<List<CotizacionDto>> = observar<List<CotizacionDto>>(C_COTIZACIONES).map { it.orEmpty() }
    val ruta: Flow<RutaResponse?> = observar<RutaResponse>(C_RUTA)
    val notificaciones: Flow<List<NotificacionDto>> = observar<List<NotificacionDto>>(C_NOTIFICACIONES).map { it.orEmpty() }
    fun cliente(id: Int): Flow<ClienteDto?> = observar<ClienteDto>("cliente_$id")
    fun recomendaciones(clienteId: Int): Flow<List<RecomendacionDto>> = observar<List<RecomendacionDto>>("recom_$clienteId").map { it.orEmpty() }
    fun historial(leadId: Int): Flow<List<HistorialDto>> = observar<List<HistorialDto>>("historial_$leadId").map { it.orEmpty() }

    // ================================================================ descargas
    suspend fun descargarTodo() {
        refrescarProductos()
        refrescarCotizaciones()
        refrescarRuta()
        refrescarNotificaciones()
    }

    suspend fun refrescarProductos(): String? = when (val r = llamarApi { api.productos() }) {
        is Resultado.Ok -> { guardar(C_PRODUCTOS, r.dato.datos.orEmpty()); null }
        is Resultado.Error -> r.mensaje
    }

    suspend fun refrescarCotizaciones(): String? = when (val r = llamarApi { api.cotizaciones() }) {
        is Resultado.Ok -> { guardar(C_COTIZACIONES, r.dato.datos.orEmpty()); null }
        is Resultado.Error -> r.mensaje
    }

    suspend fun refrescarRuta(): String? = when (val r = llamarApi { api.rutaDelDia(Formato.hoyIso()) }) {
        is Resultado.Ok -> { guardar(C_RUTA, r.dato); null }
        is Resultado.Error -> r.mensaje
    }

    suspend fun refrescarCliente(clienteId: Int): String? {
        val r = llamarApi { api.cliente(clienteId) }
        val rec = llamarApi { api.recomendaciones(clienteId) }
        if (rec is Resultado.Ok) guardar("recom_$clienteId", rec.dato.datos.orEmpty())
        return when (r) {
            is Resultado.Ok -> { r.dato.dato?.let { guardar("cliente_$clienteId", it) }; null }
            is Resultado.Error -> r.mensaje
        }
    }

    suspend fun refrescarHistorial(leadId: Int): String? = when (val r = llamarApi { api.historialLead(leadId) }) {
        is Resultado.Ok -> { guardar("historial_$leadId", r.dato.datos.orEmpty()); null }
        is Resultado.Error -> r.mensaje
    }

    // Descarga las notificaciones y muestra en el sistema las que son nuevas.
    suspend fun refrescarNotificaciones(): String? = when (val r = llamarApi { api.notificaciones() }) {
        is Resultado.Ok -> {
            val lista = r.dato.datos.orEmpty()
            guardar(C_NOTIFICACIONES, lista)
            val maximo = lista.maxOfOrNull { it.id } ?: 0L
            val ultima = sesion.ultimaNotificacion
            if (ultima < 0) sesion.ultimaNotificacion = maximo
            else {
                lista.filter { it.id > ultima && it.leida != true }.sortedBy { it.id }.takeLast(5).forEach {
                    Notificador.mostrar(context, it.id.toInt(), it.titulo ?: "CRM Andino", it.cuerpo ?: "")
                }
                if (maximo > ultima) sesion.ultimaNotificacion = maximo
            }
            null
        }
        is Resultado.Error -> r.mensaje
    }

    suspend fun marcarLeidas() {
        if (llamarApi { api.marcarLeidas(MarcarLeidasRequest(null)) } is Resultado.Ok) refrescarNotificaciones()
    }

    // ================================================================ escrituras (cola)
    // RF-V09/V10: crea la cotización y la envía; si el descuento pasa de 10 % queda para aprobación del supervisor.
    suspend fun crearCotizacion(lead: Lead, carrito: List<ItemCarrito>, descuento: Double) {
        val req = CotizacionRequest(
            uuid = UUID.randomUUID().toString(),
            leadUuid = lead.uuid,
            descuentoPct = descuento,
            items = carrito.filter { it.cantidad > 0 }.map { ItemCotizacionRequest(it.producto.id, it.cantidad) },
            enviar = true
        )
        val total = carrito.sumOf { it.subtotal } * (1 - descuento / 100)
        db.withTransaction {
            cola.registrar(
                EntidadSync.COTIZACION, 0, TipoOperacion.CREAR, req,
                "Cotización para \"${lead.empresa}\" · ${Formato.soles(total)}" + if (descuento > DESCUENTO_MAXIMO) " (requiere aprobación)" else ""
            )
            if (descuento <= DESCUENTO_MAXIMO) {
                db.leadDao().porUuid(lead.uuid)?.let { db.leadDao().actualizar(it.copy(etapa = EtapaVenta.COTIZADO.name)) }
            }
        }
        programarSync()
    }

    // RF-V12/V13: check-in con la ubicación GPS (y el motivo si está a más de 300 m).
    suspend fun registrarCheckin(lead: Lead, latitud: Double, longitud: Double, precision: Double?, motivo: String?, distancia: Double?) {
        val req = VisitaRequest(
            uuid = UUID.randomUUID().toString(),
            leadUuid = lead.uuid,
            fechaProgramada = Formato.hoyIso(),
            checkin = CheckinRequest(latitud, longitud, precision, Formato.ahoraIso()),
            motivoDistancia = motivo
        )
        db.withTransaction {
            cola.registrar(
                EntidadSync.VISITA, 0, TipoOperacion.CREAR, req,
                "Check-in en \"${lead.empresa}\"" + (distancia?.let { " a ${it.toInt()} m" } ?: "")
            )
            db.leadDao().porUuid(lead.uuid)?.let { db.leadDao().actualizar(it.copy(diasSinContacto = 0)) }
        }
        // La ruta guardada se marca como visitada al instante (el servidor lo confirma al sincronizar)
        cache.leer(C_RUTA)?.let { fila ->
            runCatching { gson.fromJson(fila.json, RutaResponse::class.java) }.getOrNull()?.let { r ->
                val nuevas = r.datos.orEmpty().map { v ->
                    if (v.leadUuid == lead.uuid) v.copy(checkinEn = Formato.ahoraIso(), distanciaM = distancia, motivoDistancia = motivo) else v
                }
                guardar(C_RUTA, r.copy(datos = nuevas))
            }
        }
        programarSync()
    }

    companion object {
        const val DESCUENTO_MAXIMO = 10.0     // RN-03: más de 10 % lo aprueba el supervisor
        const val RADIO_MAXIMO_M = 300.0      // RF-V13: check-in a más de 300 m necesita motivo
        private const val C_PRODUCTOS = "productos"
        private const val C_COTIZACIONES = "cotizaciones"
        private const val C_RUTA = "ruta_hoy"
        private const val C_NOTIFICACIONES = "notificaciones"

        // Distancia en metros entre dos coordenadas (fórmula de Haversine)
        fun distanciaMetros(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val r = 6_371_000.0
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2)
            return 2 * r * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        }
    }
}
