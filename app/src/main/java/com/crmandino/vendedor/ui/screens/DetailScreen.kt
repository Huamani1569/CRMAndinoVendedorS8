package com.crmandino.vendedor.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.crmandino.vendedor.model.EstadoSincronizacion
import com.crmandino.vendedor.model.EtapaVenta
import com.crmandino.vendedor.model.Interaccion
import com.crmandino.vendedor.model.Lead
import com.crmandino.vendedor.model.Tarea
import com.crmandino.vendedor.ui.components.BarraSuperior
import com.crmandino.vendedor.ui.components.EstadoCargando
import com.crmandino.vendedor.ui.components.EstadoVacio
import com.crmandino.vendedor.ui.components.EstadoSyncChip
import com.crmandino.vendedor.ui.components.EtapaChip
import com.crmandino.vendedor.ui.components.SelectorChips
import com.crmandino.vendedor.ui.components.TituloSeccion
import com.crmandino.vendedor.ui.components.fechaDentroDe
import com.crmandino.vendedor.ui.components.formatearSoles
import com.crmandino.vendedor.ui.theme.DoradoInca
import com.crmandino.vendedor.ui.theme.TurquesaLago
import com.crmandino.vendedor.viewmodel.InteraccionViewModel
import com.crmandino.vendedor.viewmodel.LeadViewModel
import com.crmandino.vendedor.viewmodel.TareaViewModel
import kotlinx.coroutines.flow.flowOf

// Detalle de un lead: datos, interacciones (RF08) y tareas; permite cambiar la etapa (RF09).
@Composable
fun DetailScreen(
    leadId: Int,
    leadViewModel: LeadViewModel,
    interaccionViewModel: InteraccionViewModel,
    tareaViewModel: TareaViewModel,
    onEditar: () -> Unit,
    onAtras: () -> Unit
) {
    val lead by remember(leadId) { leadViewModel.leadPorId(leadId) }.collectAsState(initial = null)
    // Las interacciones y tareas se relacionan con el lead por su UUID
    val leadUuid = lead?.uuid
    val interacciones by remember(leadUuid) {
        if (leadUuid == null) flowOf(emptyList<Interaccion>()) else interaccionViewModel.interaccionesDe(leadUuid)
    }.collectAsState(initial = emptyList())
    val tareas by remember(leadUuid) {
        if (leadUuid == null) flowOf(emptyList<Tarea>()) else tareaViewModel.tareasDe(leadUuid)
    }.collectAsState(initial = emptyList())

    // Diálogos abiertos
    var mostrarInteraccion by remember { mutableStateOf(false) }
    var interaccionEditando by remember { mutableStateOf<Interaccion?>(null) }
    var mostrarCambioEtapa by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = "Detalle del lead",
                onAtras = onAtras,
                acciones = {
                    IconButton(onClick = onEditar) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar lead")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        val actual = lead
        if (actual == null) {
            Column(Modifier.padding(padding)) { EstadoCargando("Cargando lead (si no aparece, ya no está asignado a ti)...") }
        } else {
            ContenidoDetalle(
                lead = actual,
                interacciones = interacciones,
                tareas = tareas,
                modifier = Modifier.padding(padding),
                onRegistrarInteraccion = {
                    interaccionViewModel.limpiarError()
                    interaccionEditando = null
                    mostrarInteraccion = true
                },
                onEditarInteraccion = { interaccion ->
                    interaccionViewModel.limpiarError()
                    interaccionEditando = interaccion
                    mostrarInteraccion = true
                },
                onEliminarInteraccion = interaccionViewModel::eliminar,
                onCambiarEtapa = {
                    leadViewModel.limpiarErrores()
                    mostrarCambioEtapa = true
                },
                onCambiarEstadoTarea = tareaViewModel::cambiarEstado,
                onEliminarTarea = tareaViewModel::eliminar
            )

            if (mostrarInteraccion) {
                InteraccionDialog(
                    inicial = interaccionEditando,
                    tipos = interaccionViewModel.tipos,
                    error = interaccionViewModel.error,
                    onGuardar = { tipo, fecha, nota ->
                        interaccionViewModel.guardar(
                            id = interaccionEditando?.id ?: 0,
                            leadUuid = actual.uuid,
                            tipo = tipo, fecha = fecha, nota = nota,
                            onListo = { mostrarInteraccion = false }
                        )
                    },
                    onCancelar = { mostrarInteraccion = false }
                )
            }

            if (mostrarCambioEtapa) {
                CambiarEtapaDialog(
                    etapaActual = actual.etapa,
                    error = leadViewModel.errorEtapa,
                    onGuardar = { etapa, recordatorio, fecha ->
                        leadViewModel.cambiarEtapa(actual, etapa, recordatorio, fecha,
                            onListo = { mostrarCambioEtapa = false })
                    },
                    onCancelar = { mostrarCambioEtapa = false }
                )
            }
        }
    }
}

@Composable
private fun ContenidoDetalle(
    lead: Lead,
    interacciones: List<Interaccion>,
    tareas: List<Tarea>,
    modifier: Modifier,
    onRegistrarInteraccion: () -> Unit,
    onEditarInteraccion: (Interaccion) -> Unit,
    onEliminarInteraccion: (Interaccion) -> Unit,
    onCambiarEtapa: () -> Unit,
    onCambiarEstadoTarea: (Tarea) -> Unit,
    onEliminarTarea: (Tarea) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { DatosLead(lead) }
        item { DatosSincronizacion(lead) }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onRegistrarInteraccion, modifier = Modifier.weight(1f)) {
                    Text("Registrar interacción")
                }
                OutlinedButton(onClick = onCambiarEtapa, modifier = Modifier.weight(1f)) {
                    Text("Cambiar etapa")
                }
            }
        }

        // ---------- Interacciones ----------
        item { TituloSeccion("Interacciones (${interacciones.size})") }
        if (interacciones.isEmpty()) {
            item { EstadoVacio("Aún no hay interacciones registradas") }
        } else {
            items(interacciones, key = { "i" + it.uuid }) { interaccion ->
                InteraccionItem(
                    interaccion = interaccion,
                    onEditar = { onEditarInteraccion(interaccion) },
                    onEliminar = { onEliminarInteraccion(interaccion) }
                )
            }
        }

        // ---------- Tareas ----------
        item { TituloSeccion("Tareas (${tareas.size})") }
        if (tareas.isEmpty()) {
            item { EstadoVacio("Este lead no tiene tareas") }
        } else {
            items(tareas, key = { "t" + it.uuid }) { tarea ->
                TareaItem(
                    tarea = tarea,
                    onCambiarEstado = { onCambiarEstadoTarea(tarea) },
                    onEliminar = { onEliminarTarea(tarea) }
                )
            }
        }
    }
}

@Composable
private fun DatosLead(lead: Lead) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(lead.empresa, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                EtapaChip(lead.etapa, Modifier.padding(start = 8.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text("Contacto: ${lead.contacto.ifBlank { "—" }}", style = MaterialTheme.typography.bodyLarge)
            Text("Vendedor: ${lead.vendedorNombre}", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(6.dp))
            Text(
                "Valor potencial: ${formatearSoles(lead.valorPotencial)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TurquesaLago
            )
        }
    }
}

// Datos de SINCRONIZACIÓN del registro (evidencia para la exposición):
// id local, id remoto, UUID, versión y estado.
@Composable
private fun DatosSincronizacion(lead: Lead) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Sincronización", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                EstadoSyncChip(lead.estadoSync)
            }
            Spacer(Modifier.height(6.dp))
            val estilo = MaterialTheme.typography.bodySmall
            val color = MaterialTheme.colorScheme.onSurfaceVariant
            Text("ID local (Room): ${lead.id}", style = estilo, color = color)
            Text("ID remoto (servidor): ${lead.remoteId ?: "aún no asignado"}", style = estilo, color = color)
            Text("UUID: ${lead.uuid}", style = estilo, color = color)
            Text("Versión confirmada por el servidor: ${lead.version}", style = estilo, color = color)
            lead.mensajeError?.let {
                Text("Motivo: $it", style = estilo, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun InteraccionItem(interaccion: Interaccion, onEditar: () -> Unit, onEliminar: () -> Unit) {
    val icono = when (interaccion.tipo) {
        "Llamada" -> Icons.Filled.Call
        "Correo" -> Icons.Filled.Email
        else -> Icons.Filled.Groups
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("${interaccion.tipo} · ${interaccion.fecha}", style = MaterialTheme.typography.titleMedium)
                Text(interaccion.nota, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (interaccion.estadoSync != EstadoSincronizacion.SINCRONIZADO) {
                    EstadoSyncChip(interaccion.estadoSync, Modifier.padding(top = 4.dp))
                }
            }
            IconButton(onClick = onEditar) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
            IconButton(onClick = onEliminar) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun TareaItem(tarea: Tarea, onCambiarEstado: () -> Unit, onEliminar: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = tarea.completada, onCheckedChange = { onCambiarEstado() })
            Column(Modifier.weight(1f)) {
                Text(
                    tarea.descripcion,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (tarea.completada) TextDecoration.LineThrough else null
                )
                Text(
                    if (tarea.completada) "Completada" else "Vence ${tarea.fechaLimite}",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (tarea.completada) TurquesaLago else DoradoInca
                )
                if (tarea.estadoSync != EstadoSincronizacion.SINCRONIZADO) {
                    EstadoSyncChip(tarea.estadoSync, Modifier.padding(top = 4.dp))
                }
            }
            IconButton(onClick = onEliminar) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar tarea", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// RF08: registrar o editar una interacción.
@Composable
private fun InteraccionDialog(
    inicial: Interaccion?,
    tipos: List<String>,
    error: String?,
    onGuardar: (tipo: String, fecha: String, nota: String) -> Unit,
    onCancelar: () -> Unit
) {
    var tipo by remember { mutableStateOf(inicial?.tipo ?: tipos.first()) }
    var fecha by remember { mutableStateOf(inicial?.fecha ?: fechaDentroDe(0)) }
    var nota by remember { mutableStateOf(inicial?.nota ?: "") }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(if (inicial == null) "Registrar interacción" else "Editar interacción") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SelectorChips(opciones = tipos, seleccionada = tipo, onSeleccionar = { tipo = it })
                OutlinedTextField(value = fecha, onValueChange = { fecha = it },
                    label = { Text("Fecha (dd/mm/aaaa)") }, singleLine = true)
                OutlinedTextField(value = nota, onValueChange = { nota = it },
                    label = { Text("Nota") })
                if (error != null) Text(error, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = { TextButton(onClick = { onGuardar(tipo, fecha, nota) }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}

// RF09: cambiar la etapa y crear un recordatorio con fecha límite.
@Composable
private fun CambiarEtapaDialog(
    etapaActual: String,
    error: String?,
    onGuardar: (etapa: String, recordatorio: String, fecha: String) -> Unit,
    onCancelar: () -> Unit
) {
    var etapa by remember { mutableStateOf(etapaActual) }
    var recordatorio by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf(fechaDentroDe(3)) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Cambiar etapa") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Nueva etapa", style = MaterialTheme.typography.labelMedium)
                SelectorChips(opciones = EtapaVenta.etiquetas, seleccionada = etapa, onSeleccionar = { etapa = it })
                Text("Recordatorio de seguimiento", style = MaterialTheme.typography.labelMedium)
                OutlinedTextField(value = recordatorio, onValueChange = { recordatorio = it },
                    label = { Text("¿Qué hay que hacer?") })
                OutlinedTextField(value = fecha, onValueChange = { fecha = it },
                    label = { Text("Fecha límite (dd/mm/aaaa)") }, singleLine = true)
                if (error != null) Text(error, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = { TextButton(onClick = { onGuardar(etapa, recordatorio, fecha) }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}
