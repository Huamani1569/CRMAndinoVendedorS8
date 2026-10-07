package com.crmandino.vendedor.data.mapper

import com.crmandino.vendedor.data.local.entities.LeadEntity
import com.crmandino.vendedor.data.remote.dto.LeadDto
import com.crmandino.vendedor.data.remote.dto.LeadRequest
import com.crmandino.vendedor.model.EstadoSincronizacion
import com.crmandino.vendedor.model.Lead

// MAPPER: convierte entre DTO (API) ⇄ Entity (Room) ⇄ modelo (pantallas).

// API → Room (el registro llega ya SINCRONIZADO)
fun LeadDto.aEntity(idLocal: Int = 0) = LeadEntity(
    id = idLocal,
    remoteId = id,
    uuid = uuid,
    empresa = empresa,
    contacto = contacto ?: "",
    valorPotencial = valorPotencial,
    etapa = etapa,
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
    valorPotencial = valorPotencial,
    etapa = etapa,
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
    valorPotencial = valorPotencial,
    etapa = etapa
)
