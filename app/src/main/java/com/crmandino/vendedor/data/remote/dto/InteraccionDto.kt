package com.crmandino.vendedor.data.remote.dto

data class InteraccionDto(
    val id: Int,
    val uuid: String,
    val leadUuid: String,
    val tipo: String,
    val fecha: String,          // aaaa-mm-dd
    val nota: String,
    val version: Int,
    val eliminado: Boolean?
)

data class InteraccionRequest(
    val uuid: String,
    val leadUuid: String,
    val tipo: String,
    val fecha: String,          // aaaa-mm-dd
    val nota: String,
    val version: Int? = null
)
