package com.crmandino.vendedor.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Schedule
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
import com.crmandino.vendedor.ui.theme.AzulAndino
import com.crmandino.vendedor.ui.theme.DoradoInca
import com.crmandino.vendedor.ui.theme.RojoError
import com.crmandino.vendedor.ui.theme.TextoSecundario
import com.crmandino.vendedor.ui.theme.TurquesaLago
import com.crmandino.vendedor.util.Formato

// Tarjeta de vidrio de un lead en la cartera: puntaje ML (color), etapa, días sin contacto y canal de origen.
@Composable
fun LeadVidrio(lead: Lead, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TarjetaVidrio(modifier = modifier, onClick = onClick, relleno = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PuntajeCirculo(lead.puntaje)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(lead.empresa, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    lead.contacto.ifBlank { "Sin contacto registrado" },
                    style = MaterialTheme.typography.bodyMedium, color = TextoSecundario, maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(Formato.soles(lead.valorPotencial, false), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AzulAndino)
                EtapaChip(lead.etapa)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            val dias = lead.diasSinContacto
            if (dias != null && lead.etapaVenta.abierta) {
                Capsula(
                    if (dias == 0) "Contactado hoy" else "$dias día(s) sin contacto",
                    if (dias >= 7) RojoError else if (dias >= 3) DoradoInca else TurquesaLago,
                    icono = Icons.Filled.Schedule
                )
            }
            when (lead.canalOrigen) {
                "PORTAL" -> Capsula("Portal", TurquesaLago, icono = Icons.Filled.Language)
                "CAMPANA" -> Capsula("Campaña", DoradoInca, icono = Icons.Filled.Campaign)
            }
            if (lead.estadoSync != EstadoSincronizacion.SINCRONIZADO) EstadoSyncChip(lead.estadoSync)
        }
    }
}
