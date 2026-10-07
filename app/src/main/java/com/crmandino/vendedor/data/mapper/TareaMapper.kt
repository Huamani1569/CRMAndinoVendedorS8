package com.crmandino.vendedor.data.mapper

import com.crmandino.vendedor.data.local.entities.TareaEntity
import com.crmandino.vendedor.data.remote.dto.TareaDto
import com.crmandino.vendedor.data.remote.dto.TareaRequest
import com.crmandino.vendedor.model.EstadoSincronizacion
import com.crmandino.vendedor.model.Tarea

fun TareaDto.aEntity(idLocal: Int = 0) = TareaEntity(
    id = idLocal,
    remoteId = id,
    uuid = uuid,
    leadUuid = leadUuid,
    leadEmpresa = leadEmpresa ?: "",
    descripcion = descripcion,
    fechaLimite = FechaMapper.isoAVisible(fechaLimite),
    completada = completada,
    vendedorId = vendedorId ?: 0,
    vendedorNombre = vendedorNombre ?: "",
    version = version,
    estadoSync = EstadoSincronizacion.SINCRONIZADO.name,
    mensajeError = null,
    eliminado = eliminado ?: false
)

fun TareaEntity.aModelo() = Tarea(
    id = id,
    remoteId = remoteId,
    uuid = uuid,
    leadUuid = leadUuid,
    leadEmpresa = leadEmpresa,
    descripcion = descripcion,
    fechaLimite = fechaLimite,
    completada = completada,
    vendedorNombre = vendedorNombre,
    version = version,
    estadoSync = EstadoSincronizacion.desdeTexto(estadoSync),
    mensajeError = mensajeError
)

fun TareaEntity.aRequest() = TareaRequest(
    uuid = uuid,
    leadUuid = leadUuid,
    descripcion = descripcion,
    fechaLimite = FechaMapper.visibleAIso(fechaLimite),
    completada = completada
)
