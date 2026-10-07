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
}
