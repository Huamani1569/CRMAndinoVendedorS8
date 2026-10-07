package com.crmandino.vendedor.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.crmandino.vendedor.model.EstadoSincronizacion

// Tarea / recordatorio de seguimiento de un lead.
@Entity(
    tableName = "tareas",
    indices = [Index(value = ["uuid"], unique = true), Index(value = ["leadUuid"])]
)
data class TareaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val remoteId: Int? = null,
    val uuid: String,
    val leadUuid: String,
    val leadEmpresa: String = "",
    val descripcion: String,
    val fechaLimite: String,      // dd/mm/aaaa
    val completada: Boolean = false,
    val vendedorId: Int = 0,
    val vendedorNombre: String = "",
    val version: Int = 0,
    val estadoSync: String = EstadoSincronizacion.PENDIENTE.name,
    val mensajeError: String? = null,
    val eliminado: Boolean = false
)
