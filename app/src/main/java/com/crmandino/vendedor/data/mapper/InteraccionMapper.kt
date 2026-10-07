package com.crmandino.vendedor.data.mapper

import com.crmandino.vendedor.data.local.entities.InteraccionEntity
import com.crmandino.vendedor.data.remote.dto.InteraccionDto
import com.crmandino.vendedor.data.remote.dto.InteraccionRequest
import com.crmandino.vendedor.model.EstadoSincronizacion
import com.crmandino.vendedor.model.Interaccion

fun InteraccionDto.aEntity(idLocal: Int = 0) = InteraccionEntity(
    id = idLocal,
    remoteId = id,
    uuid = uuid,
    leadUuid = leadUuid,
    tipo = tipo,
    fecha = FechaMapper.isoAVisible(fecha),
    nota = nota,
    version = version,
    estadoSync = EstadoSincronizacion.SINCRONIZADO.name,
    mensajeError = null,
    eliminado = eliminado ?: false
)

fun InteraccionEntity.aModelo() = Interaccion(
    id = id,
    remoteId = remoteId,
    uuid = uuid,
    leadUuid = leadUuid,
    tipo = tipo,
    fecha = fecha,
    nota = nota,
    version = version,
    estadoSync = EstadoSincronizacion.desdeTexto(estadoSync),
    mensajeError = mensajeError
)

fun InteraccionEntity.aRequest() = InteraccionRequest(
    uuid = uuid,
    leadUuid = leadUuid,
    tipo = tipo,
    fecha = FechaMapper.visibleAIso(fecha),
    nota = nota
)
