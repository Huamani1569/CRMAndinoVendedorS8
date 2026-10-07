package com.crmandino.vendedor.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.crmandino.vendedor.model.EstadoSincronizacion

// Interacción (llamada, correo o reunión) asociada a un lead por su uuid.
// Se usa el uuid del lead (no el id) porque un lead creado sin conexión todavía no tiene id remoto.
@Entity(
    tableName = "interacciones",
    indices = [Index(value = ["uuid"], unique = true), Index(value = ["leadUuid"])]
)
data class InteraccionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val remoteId: Int? = null,
    val uuid: String,
    val leadUuid: String,
    val tipo: String,
    val fecha: String,            // dd/mm/aaaa (en la API viaja como aaaa-mm-dd)
    val nota: String,
    val version: Int = 0,
    val estadoSync: String = EstadoSincronizacion.PENDIENTE.name,
    val mensajeError: String? = null,
    val eliminado: Boolean = false
)
