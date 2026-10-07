package com.crmandino.vendedor.data.repository

import com.crmandino.vendedor.data.local.dao.OperacionPendienteDao
import com.crmandino.vendedor.data.local.entities.OperacionPendienteEntity
import com.google.gson.Gson
import java.util.UUID

// Registra una operación en la cola local (operaciones_pendientes).
// IMPORTANTE: los repositorios la llaman DENTRO de db.withTransaction { } junto con el cambio
// del dato, así ambos se guardan juntos o no se guarda ninguno.
class ColaOperaciones(private val dao: OperacionPendienteDao) {

    private val gson = Gson()

    suspend fun registrar(
        entidad: String,
        idEntidadLocal: Int,
        tipoOperacion: String,
        datos: Any,
        descripcion: String
    ) {
        dao.insertar(
            OperacionPendienteEntity(
                uuidOperacion = UUID.randomUUID().toString(), // identificador único de ESTA operación
                entidad = entidad,
                idEntidadLocal = idEntidadLocal,
                tipoOperacion = tipoOperacion,
                payload = gson.toJson(datos),
                descripcion = descripcion
            )
        )
    }
}
