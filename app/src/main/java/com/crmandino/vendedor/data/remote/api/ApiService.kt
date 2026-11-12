package com.crmandino.vendedor.data.remote.api

import com.crmandino.vendedor.data.remote.dto.DatoResponse
import com.crmandino.vendedor.data.remote.dto.InteraccionDto
import com.crmandino.vendedor.data.remote.dto.InteraccionRequest
import com.crmandino.vendedor.data.remote.dto.LeadDto
import com.crmandino.vendedor.data.remote.dto.LeadRequest
import com.crmandino.vendedor.data.remote.dto.ListaResponse
import com.crmandino.vendedor.data.remote.dto.LoginRequest
import com.crmandino.vendedor.data.remote.dto.LoginResponse
import com.crmandino.vendedor.data.remote.dto.SaludResponse
import com.crmandino.vendedor.data.remote.dto.TareaDto
import com.crmandino.vendedor.data.remote.dto.TareaRequest
import com.crmandino.vendedor.data.remote.dto.ClienteDto
import com.crmandino.vendedor.data.remote.dto.CotizacionDto
import com.crmandino.vendedor.data.remote.dto.CotizacionRequest
import com.crmandino.vendedor.data.remote.dto.EtapaRequest
import com.crmandino.vendedor.data.remote.dto.HistorialDto
import com.crmandino.vendedor.data.remote.dto.MarcadasResponse
import com.crmandino.vendedor.data.remote.dto.MarcarLeidasRequest
import com.crmandino.vendedor.data.remote.dto.NotificacionesResponse
import com.crmandino.vendedor.data.remote.dto.ProductoDto
import com.crmandino.vendedor.data.remote.dto.RecomendacionDto
import com.crmandino.vendedor.data.remote.dto.RutaResponse
import com.crmandino.vendedor.data.remote.dto.VisitaDto
import com.crmandino.vendedor.data.remote.dto.VisitaRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// Endpoints de la API REST que usa la app VENDEDOR (solo los permitidos para su rol).
// Toda escritura envía la cabecera X-Operacion-UUID para que el servidor evite duplicados.
interface ApiService {

    @GET("api/salud")
    suspend fun salud(): Response<SaludResponse>

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    // ---------- Leads ----------
    @GET("api/leads")
    suspend fun obtenerLeads(): Response<ListaResponse<LeadDto>>

    @POST("api/leads")
    suspend fun crearLead(
        @Header("X-Operacion-UUID") uuidOperacion: String,
        @Body body: LeadRequest
    ): Response<DatoResponse<LeadDto>>

    @PUT("api/leads/{id}")
    suspend fun actualizarLead(
        @Header("X-Operacion-UUID") uuidOperacion: String,
        @Path("id") id: Int,
        @Body body: LeadRequest
    ): Response<DatoResponse<LeadDto>>

    // ---------- Interacciones ----------
    @GET("api/interacciones")
    suspend fun obtenerInteracciones(): Response<ListaResponse<InteraccionDto>>

    @POST("api/interacciones")
    suspend fun crearInteraccion(
        @Header("X-Operacion-UUID") uuidOperacion: String,
        @Body body: InteraccionRequest
    ): Response<DatoResponse<InteraccionDto>>

    @PUT("api/interacciones/{id}")
    suspend fun actualizarInteraccion(
        @Header("X-Operacion-UUID") uuidOperacion: String,
        @Path("id") id: Int,
        @Body body: InteraccionRequest
    ): Response<DatoResponse<InteraccionDto>>

    @DELETE("api/interacciones/{id}")
    suspend fun eliminarInteraccion(
        @Header("X-Operacion-UUID") uuidOperacion: String,
        @Path("id") id: Int,
        @Query("version") version: Int
    ): Response<DatoResponse<InteraccionDto>>

    // ---------- Tareas ----------
    @GET("api/tareas")
    suspend fun obtenerTareas(): Response<ListaResponse<TareaDto>>

    @POST("api/tareas")
    suspend fun crearTarea(
        @Header("X-Operacion-UUID") uuidOperacion: String,
        @Body body: TareaRequest
    ): Response<DatoResponse<TareaDto>>

    @PUT("api/tareas/{id}")
    suspend fun actualizarTarea(
        @Header("X-Operacion-UUID") uuidOperacion: String,
        @Path("id") id: Int,
        @Body body: TareaRequest
    ): Response<DatoResponse<TareaDto>>

    @DELETE("api/tareas/{id}")
    suspend fun eliminarTarea(
        @Header("X-Operacion-UUID") uuidOperacion: String,
        @Path("id") id: Int,
        @Query("version") version: Int
    ): Response<DatoResponse<TareaDto>>

    // ---------- Proyecto Final: etapa del lead (con validación de transición en el servidor) ----------
    @PUT("api/leads/{id}/etapa")
    suspend fun cambiarEtapa(
        @Header("X-Operacion-UUID") uuidOperacion: String,
        @Path("id") id: Int,
        @Body body: EtapaRequest
    ): Response<DatoResponse<LeadDto>>

    @GET("api/leads/{id}/historial")
    suspend fun historialLead(@Path("id") id: Int): Response<ListaResponse<HistorialDto>>

    // ---------- Semana 12: catálogo y cotizaciones ----------
    @GET("api/productos")
    suspend fun productos(): Response<ListaResponse<ProductoDto>>

    @GET("api/cotizaciones")
    suspend fun cotizaciones(): Response<ListaResponse<CotizacionDto>>

    @POST("api/cotizaciones")
    suspend fun crearCotizacion(
        @Header("X-Operacion-UUID") uuidOperacion: String,
        @Body body: CotizacionRequest
    ): Response<DatoResponse<CotizacionDto>>

    @POST("api/cotizaciones/{id}/envio")
    suspend fun enviarCotizacion(
        @Header("X-Operacion-UUID") uuidOperacion: String,
        @Path("id") id: Int
    ): Response<DatoResponse<CotizacionDto>>

    // ---------- Semana 12: ruta del día y check-in con GPS ----------
    @GET("api/visitas/ruta")
    suspend fun rutaDelDia(@Query("fecha") fecha: String?): Response<RutaResponse>

    @POST("api/visitas")
    suspend fun registrarVisita(
        @Header("X-Operacion-UUID") uuidOperacion: String,
        @Body body: VisitaRequest
    ): Response<DatoResponse<VisitaDto>>

    // ---------- Semana 14: ficha del cliente y recomendaciones (ML) ----------
    @GET("api/clientes/{id}")
    suspend fun cliente(@Path("id") id: Int): Response<DatoResponse<ClienteDto>>

    @GET("api/ml/recomendaciones/{clienteId}")
    suspend fun recomendaciones(@Path("clienteId") clienteId: Int): Response<ListaResponse<RecomendacionDto>>

    // ---------- Notificaciones ----------
    @GET("api/notificaciones")
    suspend fun notificaciones(): Response<NotificacionesResponse>

    @PUT("api/notificaciones/leidas")
    suspend fun marcarLeidas(@Body body: MarcarLeidasRequest): Response<MarcadasResponse>
}
