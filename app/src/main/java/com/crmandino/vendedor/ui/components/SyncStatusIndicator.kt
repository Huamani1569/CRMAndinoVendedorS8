package com.crmandino.vendedor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crmandino.vendedor.model.EstadoSincronizacion
import com.crmandino.vendedor.ui.theme.DoradoInca
import com.crmandino.vendedor.ui.theme.RojoError
import com.crmandino.vendedor.ui.theme.TurquesaLago

// Color de cada estado de sincronización
fun EstadoSincronizacion.color(): Color = when (this) {
    EstadoSincronizacion.PENDIENTE -> DoradoInca
    EstadoSincronizacion.ENVIANDO -> Color(0xFF3A7BD5)
    EstadoSincronizacion.SINCRONIZADO -> TurquesaLago
    EstadoSincronizacion.ERROR -> RojoError
}

// Etiqueta pequeña con el estado de sincronización de un registro.
@Composable
fun EstadoSyncChip(estado: EstadoSincronizacion, modifier: Modifier = Modifier) {
    Text(
        text = estado.etiqueta,
        color = Color.White,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .background(estado.color(), RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

// Tarjeta del inicio: conexión, pendientes, errores, última sincronización y botón manual (RF13, RF14).
@Composable
fun PanelSincronizacion(
    conectado: Boolean,
    pendientes: Int,
    errores: Int,
    ultimaSincronizacion: Long?,
    sincronizando: Boolean,
    onSincronizar: () -> Unit,
    onVerDetalle: () -> Unit
) {
    val (icono, color, titulo) = when {
        !conectado -> Triple(Icons.Filled.CloudOff, RojoError, "Sin conexión")
        errores > 0 -> Triple(Icons.Filled.ErrorOutline, RojoError, "$errores operación(es) con error")
        pendientes > 0 -> Triple(Icons.Filled.CloudSync, DoradoInca, "$pendientes cambio(s) por sincronizar")
        else -> Triple(Icons.Filled.CloudDone, TurquesaLago, "Todo sincronizado")
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onVerDetalle),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(titulo, style = MaterialTheme.typography.titleMedium, color = color)
                Text(
                    "Pendientes: $pendientes · Errores: $errores",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Última sincronización: ${formatearFechaHora(ultimaSincronizacion)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (sincronizando) {
                CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 3.dp)
            } else {
                Button(onClick = onSincronizar) { Text("Sincronizar") }
            }
        }
    }
}
