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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crmandino.vendedor.model.EstadoSincronizacion
import com.crmandino.vendedor.model.OperacionSync
import com.crmandino.vendedor.ui.components.BarraSuperior
import com.crmandino.vendedor.ui.components.EstadoSyncChip
import com.crmandino.vendedor.ui.components.EstadoVacio
import com.crmandino.vendedor.ui.components.PanelSincronizacion
import com.crmandino.vendedor.ui.components.TituloSeccion
import com.crmandino.vendedor.ui.components.formatearFechaHora
import com.crmandino.vendedor.viewmodel.SyncViewModel

// Pantalla de SINCRONIZACIÓN (RF13, RF14, RF15): cola local de operaciones con su estado,
// errores/conflictos rechazados por el servidor y acciones para resolverlos.
@Composable
fun SyncScreen(syncViewModel: SyncViewModel, onAtras: () -> Unit) {
    val conectado by syncViewModel.conectado.collectAsState()
    val pendientes by syncViewModel.pendientes.collectAsState(initial = 0)
    val errores by syncViewModel.errores.collectAsState(initial = 0)
    val ultima by syncViewModel.ultimaSincronizacion.collectAsState(initial = null)
    val operaciones by syncViewModel.operaciones.collectAsState(initial = emptyList())

    val conError = operaciones.filter { it.estado == EstadoSincronizacion.ERROR }
    val enCola = operaciones.filter { it.estado == EstadoSincronizacion.PENDIENTE || it.estado == EstadoSincronizacion.ENVIANDO }
    val historial = operaciones.filter { it.estado == EstadoSincronizacion.SINCRONIZADO }

    Scaffold(
        topBar = { BarraSuperior(titulo = "Sincronización", subtitulo = "Cola local ⇄ API REST", onAtras = onAtras) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                PanelSincronizacion(
                    conectado = conectado,
                    pendientes = pendientes,
                    errores = errores,
                    ultimaSincronizacion = ultima,
                    sincronizando = syncViewModel.sincronizando,
                    onSincronizar = syncViewModel::sincronizarAhora,
                    onVerDetalle = {}
                )
            }
            syncViewModel.mensaje?.let { mensaje ->
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(mensaje, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            TextButton(onClick = syncViewModel::limpiarMensaje) { Text("OK") }
                        }
                    }
                }
            }

            // Rechazadas por el servidor (conflictos, permisos, validaciones)
            item { TituloSeccion("Errores y conflictos (${conError.size})") }
            if (conError.isEmpty()) {
                item { EstadoVacio("No hay operaciones rechazadas") }
            } else {
                items(conError, key = { "e" + it.id }) { op ->
                    OperacionCard(op) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { syncViewModel.descartar(op) }) { Text("Descartar mi cambio") }
                            OutlinedButton(onClick = { syncViewModel.reintentar(op) }) { Text("Reintentar") }
                        }
                    }
                }
            }

            // Pendientes de envío (sin conexión o servidor caído)
            item { TituloSeccion("Pendientes de envío (${enCola.size})") }
            if (enCola.isEmpty()) {
                item { EstadoVacio("No hay operaciones pendientes") }
            } else {
                items(enCola, key = { "p" + it.id }) { op -> OperacionCard(op) {} }
            }

            // Historial: aceptadas por el servidor (con botón para la prueba de duplicados)
            item { TituloSeccion("Sincronizadas recientemente (${historial.size})") }
            if (historial.isEmpty()) {
                item { EstadoVacio("Todavía no hay operaciones enviadas") }
            } else {
                items(historial, key = { "s" + it.id }) { op ->
                    OperacionCard(op) {
                        TextButton(onClick = { syncViewModel.probarDuplicado(op) }) {
                            Text("Reenviar (prueba de duplicado)")
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun OperacionCard(op: OperacionSync, acciones: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    op.descripcion.ifBlank { "${op.tipoOperacion} ${op.entidad}" },
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                EstadoSyncChip(op.estado)
            }
            val estilo = MaterialTheme.typography.bodySmall
            val color = MaterialTheme.colorScheme.onSurfaceVariant
            Text("${op.tipoOperacion} · ${op.entidad} · registrada ${formatearFechaHora(op.fechaRegistro)}", style = estilo, color = color)
            Text("UUID operación: ${op.uuidOperacion}", style = estilo, color = color)
            Text(
                "Intentos: ${op.intentos}" + (op.codigoHttp?.let { " · Respuesta HTTP $it" } ?: ""),
                style = estilo,
                color = color
            )
            op.mensajeError?.let {
                val esError = op.estado == EstadoSincronizacion.ERROR
                Text(
                    it,
                    style = estilo,
                    fontWeight = FontWeight.Medium,
                    color = if (esError) MaterialTheme.colorScheme.error else color
                )
            }
            acciones()
        }
    }
}
