package com.crmandino.vendedor.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.crmandino.vendedor.model.EstadoSincronizacion
import com.crmandino.vendedor.model.RolUsuario
import com.crmandino.vendedor.model.Tarea
import com.crmandino.vendedor.model.UsuarioSesion
import com.crmandino.vendedor.ui.components.AccesoRapido
import com.crmandino.vendedor.ui.components.BarraSuperior
import com.crmandino.vendedor.ui.components.EmbudoVentas
import com.crmandino.vendedor.ui.components.EstadoSyncChip
import com.crmandino.vendedor.ui.components.EstadoVacio
import com.crmandino.vendedor.ui.components.PanelSincronizacion
import com.crmandino.vendedor.ui.components.ResumenCard
import com.crmandino.vendedor.ui.components.TituloSeccion
import com.crmandino.vendedor.ui.components.formatearSoles
import com.crmandino.vendedor.ui.theme.DoradoInca
import com.crmandino.vendedor.ui.theme.RojoError
import com.crmandino.vendedor.ui.theme.TurquesaLago
import com.crmandino.vendedor.viewmodel.LeadViewModel
import com.crmandino.vendedor.viewmodel.SyncViewModel
import com.crmandino.vendedor.viewmodel.TareaViewModel

// Inicio del VENDEDOR: estado de sincronización, indicadores, embudo (Canvas) y tareas pendientes.
@Composable
fun HomeScreen(
    usuario: UsuarioSesion,
    leadViewModel: LeadViewModel,
    tareaViewModel: TareaViewModel,
    syncViewModel: SyncViewModel,
    avisoSinConexion: String?,
    onDescartarAviso: () -> Unit,
    onVerLeads: (etapa: String?) -> Unit,
    onNuevoLead: () -> Unit,
    onAbrirLead: (Int) -> Unit,
    onVerSincronizacion: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val leads by leadViewModel.leads.collectAsState(initial = emptyList())
    val valorEnEmbudo by leadViewModel.valorEnEmbudo.collectAsState(initial = 0.0)
    val resumen by leadViewModel.resumenPorEtapa.collectAsState(initial = emptyList())
    val pendientes by tareaViewModel.pendientes.collectAsState(initial = emptyList())
    val conectado by syncViewModel.conectado.collectAsState()
    val opPendientes by syncViewModel.pendientes.collectAsState(initial = 0)
    val opErrores by syncViewModel.errores.collectAsState(initial = 0)
    val ultimaSync by syncViewModel.ultimaSincronizacion.collectAsState(initial = null)
    val idPorUuid = leads.associate { it.uuid to it.id }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Hola, ${usuario.nombre.substringBefore(" ")}",
                subtitulo = RolUsuario.ETIQUETA,
                acciones = {
                    IconButton(onClick = onVerSincronizacion) {
                        Icon(Icons.Filled.Sync, contentDescription = "Sincronización")
                    }
                    IconButton(onClick = onCerrarSesion) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (avisoSinConexion != null) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = DoradoInca.copy(alpha = 0.2f))) {
                        Column(Modifier.padding(12.dp)) {
                            Text(avisoSinConexion, style = MaterialTheme.typography.bodyMedium)
                            TextButton(onClick = onDescartarAviso) { Text("Entendido") }
                        }
                    }
                }
            }

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
            syncViewModel.mensaje?.let { mensaje ->
                item {
                    Text(mensaje, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Indicadores
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ResumenCard(Icons.Filled.Groups, "${leads.size}", "Leads",
                        MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                    ResumenCard(Icons.Filled.MonetizationOn, formatearSoles(valorEnEmbudo),
                        "En embudo", TurquesaLago, Modifier.weight(1.5f))
                    ResumenCard(Icons.Filled.Alarm, "${pendientes.size}", "Pendientes",
                        DoradoInca, Modifier.weight(1f))
                }
            }

            // Accesos rápidos
            item {
                Column {
                    TituloSeccion("Accesos rápidos")
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AccesoRapido(Icons.Filled.PersonSearch, "Ver leads", { onVerLeads(null) }, Modifier.weight(1f))
                        AccesoRapido(Icons.Filled.PersonAdd, "Nuevo lead", onNuevoLead, Modifier.weight(1f))
                    }
                }
            }

            // Embudo de ventas dibujado con Canvas (tocar una etapa abre el listado filtrado)
            item { TituloSeccion("Mi embudo de ventas") }
            item {
                if (leads.isEmpty()) {
                    EstadoVacio("Aún no hay datos descargados. Conéctate y pulsa \"Sincronizar\".")
                } else {
                    EmbudoVentas(resumen, onElegirEtapa = { onVerLeads(it) })
                }
            }

            // Tareas pendientes
            item { TituloSeccion("Tareas pendientes") }
            if (pendientes.isEmpty()) {
                item { EstadoVacio("No tienes tareas pendientes") }
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

@Composable
private fun TareaPendienteItem(
    tarea: Tarea,
    onCompletar: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Event, contentDescription = null, tint = DoradoInca)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(tarea.descripcion, style = MaterialTheme.typography.titleMedium)
                Text(tarea.leadEmpresa.ifBlank { "—" }, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Vence ${tarea.fechaLimite}", style = MaterialTheme.typography.labelMedium, color = RojoError)
                    if (tarea.estadoSync != EstadoSincronizacion.SINCRONIZADO) {
                        Spacer(Modifier.width(8.dp))
                        EstadoSyncChip(tarea.estadoSync)
                    }
                }
            }
            IconButton(onClick = onCompletar) {
                Icon(Icons.Filled.CheckCircleOutline, contentDescription = "Marcar como completada",
                    tint = TurquesaLago)
            }
        }
    }
}
