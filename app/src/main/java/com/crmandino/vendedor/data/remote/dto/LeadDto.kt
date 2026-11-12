package com.crmandino.vendedor.data.remote.dto

import com.crmandino.vendedor.model.FactorPuntaje

// Lead tal como lo devuelve la API (Proyecto Final: etapa, puntaje ML, ubicación y canal de origen)
data class LeadDto(
    val id: Int,
    val uuid: String,
    val empresa: String,
    val contacto: String?,
    val telefono: String?,
    val correo: String?,
    val ruc: String?,
    val valorPotencial: Double,
    val etapa: String,
    val canalOrigen: String?,
    val codigoSeguimiento: String?,
    val puntaje: Int?,
    val puntajeFactores: List<FactorPuntaje>?,
    val diasSinContacto: Int?,
    val motivoPerdida: String?,
    val clienteId: Int?,
    val direccion: String?,
    val latitud: Double?,
    val longitud: Double?,
    val comentarioCliente: String?,
    val vendedorId: Int,
    val vendedorNombre: String?,
    val version: Int,
    val eliminado: Boolean?
)

// Lead que se envía en POST / PUT (version = la que conocía el celular, para detectar conflictos)
data class LeadRequest(
    val uuid: String,
    val empresa: String,
    val contacto: String,
    val telefono: String?,
    val correo: String?,
    val ruc: String?,
    val direccion: String?,
    val latitud: Double?,
    val longitud: Double?,
    val valorPotencial: Double,
    val etapa: String,
    val version: Int? = null
)

// PUT /api/leads/{id}/etapa
data class EtapaRequest(val etapa: String, val motivoPerdida: String?, val version: Int? = null)
