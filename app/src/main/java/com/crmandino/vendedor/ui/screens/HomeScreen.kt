package com.crmandino.vendedor.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.crmandino.vendedor.model.EstadoSincronizacion
import com.crmandino.vendedor.model.RolUsuario
import com.crmandino.vendedor.model.Tarea
import com.crmandino.vendedor.model.UsuarioSesion
import com.crmandino.vendedor.ui.components.AccionIcono
import com.crmandino.vendedor.ui.components.BarraVidrio
import com.crmandino.vendedor.ui.components.BotonCircular
import com.crmandino.vendedor.ui.components.EmbudoVentas
import com.crmandino.vendedor.ui.components.EstadoSyncChip
import com.crmandino.vendedor.ui.components.FondoAndino
import com.crmandino.vendedor.ui.components.IconoCirculo
import com.crmandino.vendedor.ui.components.KpiVidrio
import com.crmandino.vendedor.ui.components.LeadVidrio
import com.crmandino.vendedor.ui.components.MensajeVidrio
import com.crmandino.vendedor.ui.components.PanelSincronizacion
import com.crmandino.vendedor.ui.components.Seccion
import com.crmandino.vendedor.ui.components.TarjetaVidrio
import com.crmandino.vendedor.ui.components.VacioVidrio
import com.crmandino.vendedor.ui.theme.AzulAndino
import com.crmandino.vendedor.ui.theme.DoradoInca
import com.crmandino.vendedor.ui.theme.RojoError
import com.crmandino.vendedor.ui.theme.TextoSecundario
import com.crmandino.vendedor.ui.theme.TurquesaLago
import com.crmandino.vendedor.util.Formato
import com.crmandino.vendedor.viewmodel.LeadViewModel
import com.crmandino.vendedor.viewmodel.SyncViewModel
import com.crmandino.vendedor.viewmodel.TareaViewModel
import com.crmandino.vendedor.viewmodel.VentasViewModel

// Inicio del VENDEDOR: sincronización, indicadores con ML, accesos (cartera, ruta, cotizaciones),
// leads prioritarios por puntaje, embudo (Canvas) y tareas pendientes.
@Composable
fun HomeScreen(
    usuario: UsuarioSesion,
    leadViewModel: LeadViewModel,
    tareaViewModel: TareaViewModel,
    syncViewModel: SyncViewModel,
    ventasViewModel: VentasViewModel,
    avisoSinConexion: String?,
    onDescartarAviso: () -> Unit,
    onVerLeads: (etapa: String?) -> Unit,
    onNuevoLead: () -> Unit,
    onAbrirLead: (Int) -> Unit,
    onVerRuta: () -> Unit,
    onVerCotizaciones: () -> Unit,
    onVerNotificaciones: () -> Unit,
    onVerSincronizacion: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val leads by leadViewModel.leads.collectAsState(initial = emptyList())
    val valorEsperado by leadViewModel.valorEsperado.collectAsState(initial = 0.0)
    val resumen by leadViewModel.resumenPorEtapa.collectAsState(initial = emptyList())
    val pendientes by tareaViewModel.pendientes.collectAsState(initial = emptyList())
    val conectado by syncViewModel.conectado.collectAsState()
    val opPendientes by syncViewModel.pendientes.collectAsState(initial = 0)
    val opErrores by syncViewModel.errores.collectAsState(initial = 0)
    val ultimaSync by syncViewModel.ultimaSincronizacion.collectAsState(initial = null)
    val noLeidas by ventasViewModel.noLeidas.collectAsState()
    val cotizaciones by ventasViewModel.cotizaciones.collectAsState()
    val ruta by ventasViewModel.ruta.collectAsState()
    val idPorUuid = leads.associate { it.uuid to it.id }
    val abiertos = leads.filter { it.etapaVenta.abierta }
    val prioritarios = abiertos.sortedByDescending { it.puntaje ?: -1 }.take(3)

    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33) pedirPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
        ventasViewModel.iniciarSondeo()
    }

    FondoAndino {
        Column(Modifier.fillMaxSize()) {
            BarraVidrio(
                titulo = "Hola, ${usuario.nombre.substringBefore(" ")}",
                subtitulo = RolUsuario.ETIQUETA,
                acciones = {
                    BotonCircular(Icons.Filled.Notifications, "Notificaciones", onVerNotificaciones, insignia = noLeidas)
                    BotonCircular(Icons.Filled.Sync, "Sincronización", onVerSincronizacion, insignia = opErrores)
                    BotonCircular(Icons.AutoMirrored.Filled.Logout, "Cerrar sesión", onCerrarSesion)
                }
            )
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (avisoSinConexion != null) {
                    item { MensajeVidrio(avisoSinConexion, esError = true, onCerrar = onDescartarAviso) }
                }
                ventasViewModel.mensaje?.let { item { MensajeVidrio(it, ventasViewModel.mensajeEsError, ventasViewModel::cerrarMensaje) } }

                // RF13 + RF14: estado de la sincronización y botón manual
                item {
                    PanelSincronizacion(
                        conectado = conectado,
                        pendientes = opPendientes,
                        errores = opErrores,
                        ultimaSincronizacion = ultimaSync,
                        sincronizando = syncViewModel.sincronizando,
                        onSincronizar = syncViewModel::sincronizarAhora,
                        onVerDetalle = onVerSincronizacion
                    )
                }
                syncViewModel.mensaje?.let { m ->
                    item { Text(m, style = MaterialTheme.typography.bodySmall, color = TextoSecundario) }
                }

                // Indicadores
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        KpiVidrio(Icons.Filled.Groups, "${abiertos.size}", "Leads abiertos", AzulAndino, Modifier.weight(1f), onClick = { onVerLeads(null) })
                        KpiVidrio(Icons.Filled.AutoGraph, Formato.solesCorto(valorEsperado), "Venta esperada (ML)", TurquesaLago, Modifier.weight(1.3f))
                        KpiVidrio(Icons.Filled.Alarm, "${pendientes.size}", "Tareas", DoradoInca, Modifier.weight(1f))
                    }
                }

                // Accesos rápidos
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        KpiVidrio(Icons.Filled.Map, "Ruta", "${ruta?.datos?.size ?: 0} visita(s) hoy", TurquesaLago, Modifier.weight(1f), onClick = onVerRuta)
                        KpiVidrio(Icons.Filled.ReceiptLong, "Cotizaciones", "${cotizaciones.count { it.estado == "ENVIADA" }} esperando", AzulAndino, Modifier.weight(1f), onClick = onVerCotizaciones)
                        KpiVidrio(Icons.Filled.PersonAdd, "Nuevo", "lead", DoradoInca, Modifier.weight(1f), onClick = onNuevoLead)
                    }
                }

                // Leads con mayor probabilidad de cierre (ML-01)
                item {
                    Seccion("Prioriza hoy", accion = { TextButton(onClick = { onVerLeads(null) }) { Text("Ver cartera") } })
                }
                if (prioritarios.isEmpty()) {
                    item { VacioVidrio(Icons.Filled.Groups, "Aún no hay leads descargados", "Conéctate y pulsa \"Sincronizar\".") }
                } else {
                    items(prioritarios, key = { "p" + it.uuid }) { LeadVidrio(it, onClick = { onAbrirLead(it.id) }) }
                }

                // Embudo de ventas dibujado con Canvas (tocar una etapa abre la cartera filtrada)
                if (leads.isNotEmpty()) {
                    item { Seccion("Mi embudo de ventas") }
                    item { EmbudoVentas(resumen, onElegirEtapa = { onVerLeads(it) }) }
                }

                // Tareas pendientes
                item { Seccion("Tareas pendientes") }
                if (pendientes.isEmpty()) {
                    item { VacioVidrio(Icons.Filled.CheckCircleOutline, "No tienes tareas pendientes", null) }
                } else {
                    items(pendientes, key = { it.uuid }) { tarea ->
                        TareaPendienteItem(
                            tarea = tarea,
                            onCompletar = { tareaViewModel.cambiarEstado(tarea) },
                            onClick = { idPorUuid[tarea.leadUuid]?.let(onAbrirLead) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TareaPendienteItem(tarea: Tarea, onCompletar: () -> Unit, onClick: () -> Unit) {
    TarjetaVidrio(onClick = onClick, relleno = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconoCirculo(Icons.Filled.Event, if (tarea.descripcion.startsWith("Retención", true)) RojoError else DoradoInca)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(tarea.descripcion, style = MaterialTheme.typography.titleMedium)
                Text(tarea.leadEmpresa.ifBlank { "—" }, style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Vence ${tarea.fechaLimite}", style = MaterialTheme.typography.labelMedium, color = RojoError)
                    if (tarea.estadoSync != EstadoSincronizacion.SINCRONIZADO) {
                        Spacer(Modifier.width(8.dp))
                        EstadoSyncChip(tarea.estadoSync)
                    }
                }
            }
            Box { AccionIcono(Icons.Filled.CheckCircleOutline, "Marcar como completada", onCompletar, TurquesaLago) }
        }
    }
}
