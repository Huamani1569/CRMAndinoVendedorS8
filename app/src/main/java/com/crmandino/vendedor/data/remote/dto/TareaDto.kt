package com.crmandino.vendedor.data.remote.dto

data class TareaDto(
    val id: Int,
    val uuid: String,
    val leadUuid: String,
    val leadEmpresa: String?,
    val descripcion: String,
    val fechaLimite: String,    // aaaa-mm-dd
    val completada: Boolean,
    val vendedorId: Int?,
    val vendedorNombre: String?,
    val version: Int,
    val eliminado: Boolean?
)

data class TareaRequest(
    val uuid: String,
    val leadUuid: String,
    val descripcion: String,
    val fechaLimite: String,    // aaaa-mm-dd
    val completada: Boolean,
    val version: Int? = null
)
