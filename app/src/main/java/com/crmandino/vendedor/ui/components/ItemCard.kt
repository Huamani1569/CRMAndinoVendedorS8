package com.crmandino.vendedor.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.crmandino.vendedor.model.EstadoSincronizacion
import com.crmandino.vendedor.model.Lead
import com.crmandino.vendedor.ui.theme.TurquesaLago

// Tarjeta de un lead: empresa, etapa, contacto, vendedor y valor potencial.
// onClick y acciones (por ejemplo, íconos de editar/eliminar) son opcionales.
@Composable
fun LeadCard(
    lead: Lead,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    acciones: @Composable RowScope.() -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    lead.empresa,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                EtapaChip(lead.etapa, Modifier.padding(start = 8.dp))
            }
            Spacer(Modifier.height(4.dp))
            Text(
                lead.contacto.ifBlank { "Sin contacto registrado" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (lead.estadoSync != EstadoSincronizacion.SINCRONIZADO) {
                    EstadoSyncChip(lead.estadoSync, Modifier.padding(end = 6.dp))
                }
                Text(
                    lead.vendedorNombre,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    formatearSoles(lead.valorPotencial),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TurquesaLago
                )
                acciones()
            }
        }
    }
}
