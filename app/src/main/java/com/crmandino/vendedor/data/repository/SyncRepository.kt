package com.crmandino.vendedor.data.repository

import androidx.room.withTransaction
import com.crmandino.vendedor.data.local.AppDatabase
import com.crmandino.vendedor.data.local.SesionLocal
import com.crmandino.vendedor.data.local.entities.OperacionPendienteEntity
import com.crmandino.vendedor.data.local.entities.SyncMetadataEntity
import com.crmandino.vendedor.data.mapper.aEntity
import com.crmandino.vendedor.data.remote.api.ApiService
import com.crmandino.vendedor.data.remote.dto.DatoResponse
import com.crmandino.vendedor.data.remote.dto.ErrorResponse
import com.crmandino.vendedor.data.remote.dto.InteraccionDto
import com.crmandino.vendedor.data.remote.dto.InteraccionRequest
import com.crmandino.vendedor.data.remote.dto.LeadDto
import com.crmandino.vendedor.data.remote.dto.LeadRequest
import com.crmandino.vendedor.data.remote.dto.TareaDto
import com.crmandino.vendedor.data.remote.dto.TareaRequest
import com.crmandino.vendedor.model.EntidadSync
import com.crmandino.vendedor.model.EstadoSincronizacion
import com.crmandino.vendedor.model.OperacionSync
import com.crmandino.vendedor.model.TipoOperacion
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.Response
import java.io.IOException
import java.util.UUID

// Resultado de un intento de sincronización (lo muestran la pantalla y el SyncWorker).
data class ResultadoSync(val exito: Boolean, val mensaje: String, val reintentar: Boolean = false)

/**
 * SINCRONIZACIÓN entre Room y la API REST:
 *  1) SUBIR: envía la cola de operaciones pendientes en orden (FIFO), con su X-Operacion-UUID.
 *  2) BAJAR: descarga los datos autorizados y actualiza Room SIN pisar cambios locales pendientes.
 *  3) Guarda la fecha de la última sincronización exitosa en sync_metadata.
 * La usan el SyncWorker (WorkManager) y el botón "Sincronizar ahora".
 */
class SyncRepository(
    private val db: AppDatabase,
    private val api: ApiService,
    private val sesion: SesionLocal
) {
    private val gson = Gson()
    private val mutex = Mutex() // evita dos sincronizaciones al mismo tiempo (worker + botón)

    private val opDao = db.operacionPendienteDao()
    private val leadDao = db.leadDao()
    private val interaccionDao = db.interaccionDao()
    private val tareaDao = db.tareaDao()
    private val metaDao = db.syncMetadataDao()

    // ---------- Datos para la pantalla de sincronización ----------
    val pendientes: Flow<Int> = opDao.contarPendientes()
    val errores: Flow<Int> = opDao.contarErrores()
    val ultimaSincronizacion: Flow<Long?> = metaDao.observar(RECURSO_GENERAL).map { it?.ultimaSincronizacionExitosa }
    val operaciones: Flow<List<OperacionSync>> = opDao.observarTodas().map { lista -> lista.map { it.aModelo() } }

    suspend fun sincronizar(): ResultadoSync = mutex.withLock {
        if (sesion.token == null) {
            return ResultadoSync(false, "Inicia sesión con conexión para poder sincronizar.")
        }
        opDao.reiniciarEnviando()

        val problemaAlEnviar = enviarPendientes()
        if (problemaAlEnviar != null) return problemaAlEnviar

        val descarga = descargar()
        if (!descarga.exito) return descarga

        metaDao.guardar(SyncMetadataEntity(RECURSO_GENERAL, System.currentTimeMillis(), null))
        opDao.limpiarHistorial()
        val conError = opDao.contarSinSincronizar()
        ResultadoSync(
            exito = true,
            mensaje = if (conError > 0) "Sincronizado. $conError operación(es) rechazada(s) por el servidor: revísalas."
            else "Sincronización completa. ${descarga.mensaje}"
        )
    }

    // =====================================================================
    // 1) SUBIR la cola de operaciones
    // =====================================================================

    // Devuelve null si todo se pudo procesar; si falla la red/servidor, se detiene y lo informa.
    private suspend fun enviarPendientes(): ResultadoSync? {
        repeat(5) { // varias rondas por si se agregaron operaciones mientras se enviaba
            val cola = opDao.pendientesParaEnviar()
            if (cola.isEmpty()) return null
            for (op in cola) {
                val problema = enviarUna(op)
                if (problema != null) return problema
            }
        }
        return null
    }

    private sealed class Envio {
        data class Respuesta(
            val codigo: Int,
            val exitoso: Boolean,
            val dato: Any?,
            val duplicada: Boolean,
            val error: String?
        ) : Envio()

        data class NoEnviable(val motivo: String) : Envio()
    }

    private suspend fun enviarUna(op: OperacionPendienteEntity): ResultadoSync? {
        val enviando = op.copy(estado = EstadoSincronizacion.ENVIANDO.name)
        opDao.actualizar(enviando)
        marcarEntidad(op, EstadoSincronizacion.ENVIANDO, null)

        val envio = try {
            llamarApi(op)
        } catch (e: IOException) {
            // Sin internet, servidor apagado o tiempo de espera agotado: la operación sigue PENDIENTE.
            val mensaje = "Sin conexión con el servidor"
            opDao.actualizar(op.copy(estado = EstadoSincronizacion.PENDIENTE.name, intentos = op.intentos + 1, mensajeError = mensaje, codigoHttp = null))
            marcarEntidad(op, EstadoSincronizacion.PENDIENTE, mensaje)
            return ResultadoSync(false, "No se pudo conectar con el servidor. Los cambios siguen guardados como PENDIENTES.", reintentar = true)
        }

        when (envio) {
            is Envio.NoEnviable -> {
                opDao.actualizar(op.copy(estado = EstadoSincronizacion.ERROR.name, intentos = op.intentos + 1, mensajeError = envio.motivo))
                marcarEntidad(op, EstadoSincronizacion.ERROR, envio.motivo)
            }

            is Envio.Respuesta -> when {
                envio.exitoso -> {
                    db.withTransaction {
                        aplicarExito(op, envio.dato)
                        opDao.actualizar(
                            op.copy(
                                estado = EstadoSincronizacion.SINCRONIZADO.name,
                                intentos = op.intentos + 1,
                                codigoHttp = envio.codigo,
                                mensajeError = if (envio.duplicada) "El servidor ya la había recibido: no se duplicó" else null,
                                respuestaServidor = null
                            )
                        )
                        recalcularEstadoEntidad(op)
                    }
                }

                envio.codigo == 401 -> {
                    opDao.actualizar(op.copy(estado = EstadoSincronizacion.PENDIENTE.name, mensajeError = "Sesión expirada", codigoHttp = 401))
                    marcarEntidad(op, EstadoSincronizacion.PENDIENTE, "Sesión expirada")
                    return ResultadoSync(false, "Tu sesión expiró. Cierra sesión y vuelve a ingresar con conexión.")
                }

                envio.codigo >= 500 || envio.codigo == 408 || envio.codigo == 429 -> {
                    // Error del servidor: NO se pierde nada, se reintenta más tarde.
                    val mensaje = "Servidor no disponible (${envio.codigo})"
                    opDao.actualizar(op.copy(estado = EstadoSincronizacion.PENDIENTE.name, intentos = op.intentos + 1, mensajeError = mensaje, codigoHttp = envio.codigo))
                    marcarEntidad(op, EstadoSincronizacion.PENDIENTE, mensaje)
                    return ResultadoSync(false, "El servidor no está disponible (${envio.codigo}). Se reintentará automáticamente.", reintentar = true)
                }

                else -> {
                    // Rechazo del servidor (403 permiso, 409 conflicto, 422 validación...): queda en ERROR
                    // con el motivo, para que el usuario decida (descartar o reintentar).
                    val mensaje = mensajeDeError(envio.error) ?: "Rechazada por el servidor (${envio.codigo})"
                    opDao.actualizar(
                        op.copy(
                            estado = EstadoSincronizacion.ERROR.name,
                            intentos = op.intentos + 1,
                            mensajeError = mensaje,
                            codigoHttp = envio.codigo,
                            respuestaServidor = envio.error
                        )
                    )
                    marcarEntidad(op, EstadoSincronizacion.ERROR, mensaje)
                }
            }
        }
        return null
    }

    // Arma la petición HTTP según la entidad y el tipo de operación.
    // El id remoto y la versión se leen del registro AL MOMENTO de enviar (pueden haber cambiado).
    private suspend fun llamarApi(op: OperacionPendienteEntity): Envio {
        val uuidOp = op.uuidOperacion
        return when (op.entidad) {
            EntidadSync.LEAD -> {
                val lead = leadDao.porId(op.idEntidadLocal) ?: return Envio.NoEnviable("El lead ya no existe en el celular")
                val datos = gson.fromJson(op.payload, LeadRequest::class.java)
                when (op.tipoOperacion) {
                    TipoOperacion.CREAR -> respuesta(api.crearLead(uuidOp, datos))
                    TipoOperacion.ACTUALIZAR -> {
                        val idRemoto = lead.remoteId ?: return Envio.NoEnviable("El lead aún no existe en el servidor")
                        respuesta(api.actualizarLead(uuidOp, idRemoto, datos.copy(version = lead.version)))
                    }
                    else -> Envio.NoEnviable("Operación no permitida para el vendedor")
                }
            }

            EntidadSync.INTERACCION -> {
                val inter = interaccionDao.porId(op.idEntidadLocal) ?: return Envio.NoEnviable("La interacción ya no existe en el celular")
                val datos = gson.fromJson(op.payload, InteraccionRequest::class.java)
                when (op.tipoOperacion) {
                    TipoOperacion.CREAR -> respuesta(api.crearInteraccion(uuidOp, datos))
                    TipoOperacion.ACTUALIZAR -> {
                        val idRemoto = inter.remoteId ?: return Envio.NoEnviable("La interacción aún no existe en el servidor")
                        respuesta(api.actualizarInteraccion(uuidOp, idRemoto, datos.copy(version = inter.version)))
                    }
                    else -> {
                        val idRemoto = inter.remoteId ?: return Envio.NoEnviable("La interacción aún no existe en el servidor")
                        respuesta(api.eliminarInteraccion(uuidOp, idRemoto, inter.version))
                    }
                }
            }

            EntidadSync.TAREA -> {
                val tarea = tareaDao.porId(op.idEntidadLocal) ?: return Envio.NoEnviable("La tarea ya no existe en el celular")
                val datos = gson.fromJson(op.payload, TareaRequest::class.java)
                when (op.tipoOperacion) {
                    TipoOperacion.CREAR -> respuesta(api.crearTarea(uuidOp, datos))
                    TipoOperacion.ACTUALIZAR -> {
                        val idRemoto = tarea.remoteId ?: return Envio.NoEnviable("La tarea aún no existe en el servidor")
                        respuesta(api.actualizarTarea(uuidOp, idRemoto, datos.copy(version = tarea.version)))
                    }
                    else -> {
                        val idRemoto = tarea.remoteId ?: return Envio.NoEnviable("La tarea aún no existe en el servidor")
                        respuesta(api.eliminarTarea(uuidOp, idRemoto, tarea.version))
                    }
                }
            }

            else -> Envio.NoEnviable("Entidad desconocida: ${op.entidad}")
        }
    }

    private fun respuesta(r: Response<out DatoResponse<*>>): Envio.Respuesta {
        val cuerpo = r.body()
        return Envio.Respuesta(
            codigo = r.code(),
            exitoso = r.isSuccessful,
            dato = cuerpo?.dato,
            duplicada = cuerpo?.duplicada == true,
            error = if (r.isSuccessful) null else r.errorBody()?.string()
        )
    }

    // El servidor aceptó: se guarda el id remoto y la nueva versión (o se borra si era ELIMINAR).
    private suspend fun aplicarExito(op: OperacionPendienteEntity, dato: Any?) {
        when (op.entidad) {
            EntidadSync.LEAD -> {
                val dto = dato as? LeadDto ?: return
                val local = leadDao.porId(op.idEntidadLocal) ?: return
                leadDao.actualizar(
                    local.copy(
                        remoteId = dto.id,
                        version = maxOf(local.version, dto.version),
                        vendedorId = dto.vendedorId,
                        vendedorNombre = dto.vendedorNombre ?: local.vendedorNombre
                    )
                )
            }

            EntidadSync.INTERACCION -> {
                val local = interaccionDao.porId(op.idEntidadLocal) ?: return
                if (op.tipoOperacion == TipoOperacion.ELIMINAR) {
                    interaccionDao.eliminar(local)
                    return
                }
                val dto = dato as? InteraccionDto ?: return
                interaccionDao.actualizar(local.copy(remoteId = dto.id, version = maxOf(local.version, dto.version)))
            }

            EntidadSync.TAREA -> {
                val local = tareaDao.porId(op.idEntidadLocal) ?: return
                if (op.tipoOperacion == TipoOperacion.ELIMINAR) {
                    tareaDao.eliminar(local)
                    return
                }
                val dto = dato as? TareaDto ?: return
                tareaDao.actualizar(local.copy(remoteId = dto.id, version = maxOf(local.version, dto.version)))
            }
        }
    }

    // Si al registro ya no le quedan operaciones en cola → SINCRONIZADO; si no, sigue PENDIENTE.
    private suspend fun recalcularEstadoEntidad(op: OperacionPendienteEntity) {
        val activas = opDao.contarActivasDe(op.entidad, op.idEntidadLocal)
        val estado = if (activas > 0) EstadoSincronizacion.PENDIENTE else EstadoSincronizacion.SINCRONIZADO
        marcarEntidad(op, estado, null)
    }

    private suspend fun marcarEntidad(op: OperacionPendienteEntity, estado: EstadoSincronizacion, mensaje: String?) {
        when (op.entidad) {
            EntidadSync.LEAD -> leadDao.cambiarEstado(op.idEntidadLocal, estado.name, mensaje)
            EntidadSync.INTERACCION -> interaccionDao.cambiarEstado(op.idEntidadLocal, estado.name, mensaje)
            EntidadSync.TAREA -> tareaDao.cambiarEstado(op.idEntidadLocal, estado.name, mensaje)
        }
    }

    // =====================================================================
    // 2) BAJAR los datos autorizados (RF11)
    // =====================================================================
    private suspend fun descargar(): ResultadoSync {
        try {
            val rLeads = api.obtenerLeads()
            val rInteracciones = api.obtenerInteracciones()
            val rTareas = api.obtenerTareas()

            val fallida = listOf(rLeads, rInteracciones, rTareas).firstOrNull { !it.isSuccessful }
            if (fallida != null) {
                return when {
                    fallida.code() == 401 -> ResultadoSync(false, "Tu sesión expiró. Cierra sesión y vuelve a ingresar con conexión.")
                    fallida.code() >= 500 -> ResultadoSync(false, "El servidor no está disponible (${fallida.code()}). Se reintentará.", reintentar = true)
                    else -> ResultadoSync(false, "No se pudieron descargar los datos (${fallida.code()}).")
                }
            }

            val leads = rLeads.body()?.datos.orEmpty()
            val interacciones = rInteracciones.body()?.datos.orEmpty()
            val tareas = rTareas.body()?.datos.orEmpty()

            db.withTransaction {
                fusionarLeads(leads)
                fusionarInteracciones(interacciones)
                fusionarTareas(tareas)
            }

            val ahora = System.currentTimeMillis()
            metaDao.guardar(SyncMetadataEntity("leads", ahora, rLeads.body()?.servidorTiempo))
            metaDao.guardar(SyncMetadataEntity("interacciones", ahora, rInteracciones.body()?.servidorTiempo))
            metaDao.guardar(SyncMetadataEntity("tareas", ahora, rTareas.body()?.servidorTiempo))
            return ResultadoSync(true, "${leads.size} leads, ${tareas.size} tareas y ${interacciones.size} interacciones descargados.")
        } catch (e: IOException) {
            return ResultadoSync(false, "No se pudo conectar con el servidor para descargar los datos.", reintentar = true)
        }
    }

    // Regla clave: un registro local con cambios PENDIENTES o en ERROR no se sobrescribe.
    private suspend fun fusionarLeads(remotos: List<LeadDto>) {
        val uuidsRemotos = remotos.map { it.uuid }.toSet()
        for (dto in remotos) {
            val local = leadDao.porUuid(dto.uuid)
            when {
                local == null -> leadDao.insertar(dto.aEntity())
                local.estadoSync == EstadoSincronizacion.SINCRONIZADO.name -> leadDao.actualizar(dto.aEntity(local.id))
            }
        }
        // Leads que el servidor ya no devuelve (reasignados a otro vendedor o eliminados por el supervisor)
        for (local in leadDao.todos()) {
            if (local.remoteId != null && local.uuid !in uuidsRemotos && local.estadoSync == EstadoSincronizacion.SINCRONIZADO.name) {
                leadDao.eliminar(local)
            }
        }
    }

    private suspend fun fusionarInteracciones(remotas: List<InteraccionDto>) {
        val uuidsRemotos = remotas.map { it.uuid }.toSet()
        for (dto in remotas) {
            val local = interaccionDao.porUuid(dto.uuid)
            when {
                local == null -> interaccionDao.insertar(dto.aEntity())
                local.estadoSync == EstadoSincronizacion.SINCRONIZADO.name -> interaccionDao.actualizar(dto.aEntity(local.id))
            }
        }
        for (local in interaccionDao.todos()) {
            if (local.remoteId != null && local.uuid !in uuidsRemotos && local.estadoSync == EstadoSincronizacion.SINCRONIZADO.name) {
                interaccionDao.eliminar(local)
            }
        }
    }

    private suspend fun fusionarTareas(remotas: List<TareaDto>) {
        val uuidsRemotos = remotas.map { it.uuid }.toSet()
        for (dto in remotas) {
            val local = tareaDao.porUuid(dto.uuid)
            when {
                local == null -> tareaDao.insertar(dto.aEntity())
                local.estadoSync == EstadoSincronizacion.SINCRONIZADO.name -> tareaDao.actualizar(dto.aEntity(local.id))
            }
        }
        for (local in tareaDao.todos()) {
            if (local.remoteId != null && local.uuid !in uuidsRemotos && local.estadoSync == EstadoSincronizacion.SINCRONIZADO.name) {
                tareaDao.eliminar(local)
            }
        }
    }

    // =====================================================================
    // 3) RESOLVER operaciones rechazadas (RF15)
    // =====================================================================

    // "Descartar mi cambio": se quita la operación y el registro vuelve a la versión del servidor
    // en la siguiente descarga.
    suspend fun descartarCambio(idOperacion: Int) {
        mutex.withLock {
            val op = opDao.porId(idOperacion) ?: return
            db.withTransaction {
                opDao.eliminar(op)
                val sinRemoto = when (op.entidad) {
                    EntidadSync.LEAD -> leadDao.porId(op.idEntidadLocal)?.remoteId == null
                    EntidadSync.INTERACCION -> interaccionDao.porId(op.idEntidadLocal)?.remoteId == null
                    else -> tareaDao.porId(op.idEntidadLocal)?.remoteId == null
                }
                if (op.tipoOperacion == TipoOperacion.CREAR && sinRemoto) {
                    borrarEntidadLocal(op) // nunca llegó al servidor: se elimina del celular
                } else if (opDao.contarActivasDe(op.entidad, op.idEntidadLocal) == 0) {
                    restaurarEntidad(op)   // la próxima descarga trae los datos del servidor
                }
            }
        }
    }

    // "Reintentar": nueva operación (nuevo UUID) partiendo de la versión ACTUAL del servidor.
    // Así el cambio del usuario se aplica sobre lo último que hay en el servidor.
    suspend fun reintentar(idOperacion: Int) {
        mutex.withLock {
            val op = opDao.porId(idOperacion) ?: return
            val versionServidor = runCatching {
                gson.fromJson(op.respuestaServidor, ErrorResponse::class.java)?.actual?.get("version")?.asInt
            }.getOrNull()
            db.withTransaction {
                if (versionServidor != null) cambiarVersion(op, versionServidor)
                opDao.actualizar(
                    op.copy(
                        uuidOperacion = UUID.randomUUID().toString(),
                        estado = EstadoSincronizacion.PENDIENTE.name,
                        intentos = 0,
                        mensajeError = null,
                        codigoHttp = null,
                        respuestaServidor = null
                    )
                )
                marcarEntidad(op, EstadoSincronizacion.PENDIENTE, null)
            }
        }
    }

    // PRUEBA DE DUPLICADOS: reenvía una operación YA sincronizada con el MISMO X-Operacion-UUID.
    // El servidor debe responder "duplicada" sin crear otro registro.
    suspend fun reenviarComoPrueba(idOperacion: Int): String = mutex.withLock {
        val op = opDao.porId(idOperacion) ?: return "La operación ya no existe."
        try {
            when (val envio = llamarApi(op)) {
                is Envio.NoEnviable -> "No se puede reenviar: ${envio.motivo}"
                is Envio.Respuesta ->
                    if (envio.duplicada) "✔ El servidor respondió \"duplicada\" (código ${envio.codigo}): reconoció la operación ${op.uuidOperacion.take(8)}… y NO creó ni modificó otro registro."
                    else "El servidor procesó la operación como nueva (código ${envio.codigo})."
            }
        } catch (e: IOException) {
            "Sin conexión con el servidor."
        }
    }

    private suspend fun borrarEntidadLocal(op: OperacionPendienteEntity) {
        when (op.entidad) {
            EntidadSync.LEAD -> leadDao.porId(op.idEntidadLocal)?.let { leadDao.eliminar(it) }
            EntidadSync.INTERACCION -> interaccionDao.porId(op.idEntidadLocal)?.let { interaccionDao.eliminar(it) }
            EntidadSync.TAREA -> tareaDao.porId(op.idEntidadLocal)?.let { tareaDao.eliminar(it) }
        }
    }

    private suspend fun restaurarEntidad(op: OperacionPendienteEntity) {
        val sincronizado = EstadoSincronizacion.SINCRONIZADO.name
        when (op.entidad) {
            EntidadSync.LEAD -> leadDao.porId(op.idEntidadLocal)?.let {
                leadDao.actualizar(it.copy(estadoSync = sincronizado, mensajeError = null, eliminado = false))
            }
            EntidadSync.INTERACCION -> interaccionDao.porId(op.idEntidadLocal)?.let {
                interaccionDao.actualizar(it.copy(estadoSync = sincronizado, mensajeError = null, eliminado = false))
            }
            EntidadSync.TAREA -> tareaDao.porId(op.idEntidadLocal)?.let {
                tareaDao.actualizar(it.copy(estadoSync = sincronizado, mensajeError = null, eliminado = false))
            }
        }
    }

    private suspend fun cambiarVersion(op: OperacionPendienteEntity, version: Int) {
        when (op.entidad) {
            EntidadSync.LEAD -> leadDao.porId(op.idEntidadLocal)?.let { leadDao.actualizar(it.copy(version = version)) }
            EntidadSync.INTERACCION -> interaccionDao.porId(op.idEntidadLocal)?.let { interaccionDao.actualizar(it.copy(version = version)) }
            EntidadSync.TAREA -> tareaDao.porId(op.idEntidadLocal)?.let { tareaDao.actualizar(it.copy(version = version)) }
        }
    }

    private fun mensajeDeError(json: String?): String? =
        runCatching { gson.fromJson(json, ErrorResponse::class.java)?.mensaje }.getOrNull()

    private fun OperacionPendienteEntity.aModelo() = OperacionSync(
        id = id,
        uuidOperacion = uuidOperacion,
        entidad = entidad,
        tipoOperacion = tipoOperacion,
        descripcion = descripcion,
        fechaRegistro = fechaRegistro,
        estado = EstadoSincronizacion.desdeTexto(estado),
        intentos = intentos,
        mensajeError = mensajeError,
        codigoHttp = codigoHttp
    )

    companion object {
        const val RECURSO_GENERAL = "general"
    }
}
