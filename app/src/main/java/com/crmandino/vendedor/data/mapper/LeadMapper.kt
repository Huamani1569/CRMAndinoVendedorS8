package com.crmandino.vendedor.data.mapper

import com.crmandino.vendedor.data.local.entities.LeadEntity
import com.crmandino.vendedor.data.remote.dto.LeadDto
import com.crmandino.vendedor.data.remote.dto.LeadRequest
import com.crmandino.vendedor.model.EstadoSincronizacion
import com.crmandino.vendedor.model.FactorPuntaje
import com.crmandino.vendedor.model.Lead
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// MAPPER: convierte entre DTO (API) ⇄ Entity (Room) ⇄ modelo (pantallas).

private val gson = Gson()
private val tipoFactores = object : TypeToken<List<FactorPuntaje>>() {}.type

// API → Room (el registro llega ya SINCRONIZADO)
fun LeadDto.aEntity(idLocal: Int = 0) = LeadEntity(
    id = idLocal,
    remoteId = id,
    uuid = uuid,
    empresa = empresa,
    contacto = contacto ?: "",
    telefono = telefono,
    correo = correo,
    ruc = ruc,
    direccion = direccion,
    latitud = latitud,
    longitud = longitud,
    valorPotencial = valorPotencial,
    etapa = etapa,
    canalOrigen = canalOrigen,
    codigoSeguimiento = codigoSeguimiento,
    comentarioCliente = comentarioCliente,
    puntaje = puntaje,
    factoresJson = puntajeFactores?.let { gson.toJson(it) },
    diasSinContacto = diasSinContacto,
    clienteId = clienteId,
    motivoPerdida = motivoPerdida,
    vendedorId = vendedorId,
    vendedorNombre = vendedorNombre ?: "",
    version = version,
    estadoSync = EstadoSincronizacion.SINCRONIZADO.name,
    mensajeError = null,
    eliminado = eliminado ?: false
)

// Room → pantalla
fun LeadEntity.aModelo() = Lead(
    id = id,
    remoteId = remoteId,
    uuid = uuid,
    empresa = empresa,
    contacto = contacto,
    telefono = telefono,
    correo = correo,
    ruc = ruc,
    direccion = direccion,
    latitud = latitud,
    longitud = longitud,
    valorPotencial = valorPotencial,
    etapa = etapa,
    canalOrigen = canalOrigen,
    codigoSeguimiento = codigoSeguimiento,
    comentarioCliente = comentarioCliente,
    puntaje = puntaje,
    factores = factoresJson?.let { runCatching { gson.fromJson<List<FactorPuntaje>>(it, tipoFactores) }.getOrNull() }.orEmpty(),
    diasSinContacto = diasSinContacto,
    clienteId = clienteId,
    motivoPerdida = motivoPerdida,
    vendedorId = vendedorId,
    vendedorNombre = vendedorNombre,
    version = version,
    estadoSync = EstadoSincronizacion.desdeTexto(estadoSync),
    mensajeError = mensajeError
)

// Room → cuerpo de la petición (la versión se agrega al momento de enviar)
fun LeadEntity.aRequest() = LeadRequest(
    uuid = uuid,
    empresa = empresa,
    contacto = contacto,
    telefono = telefono,
    correo = correo,
    ruc = ruc,
    direccion = direccion,
    latitud = latitud,
    longitud = longitud,
    valorPotencial = valorPotencial,
    etapa = etapa
)
