package com.crmandino.vendedor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crmandino.vendedor.model.EtapaVenta
import com.crmandino.vendedor.ui.theme.EtapaCerrado
import com.crmandino.vendedor.ui.theme.EtapaContactado
import com.crmandino.vendedor.ui.theme.EtapaNegociacion
import com.crmandino.vendedor.ui.theme.EtapaPropuesta
import com.crmandino.vendedor.ui.theme.EtapaProspecto

// Color asociado a cada etapa del embudo.
fun EtapaVenta.color(): Color = when (this) {
    EtapaVenta.PROSPECTO -> EtapaProspecto
    EtapaVenta.CONTACTADO -> EtapaContactado
    EtapaVenta.PROPUESTA -> EtapaPropuesta
    EtapaVenta.NEGOCIACION -> EtapaNegociacion
    EtapaVenta.CERRADO -> EtapaCerrado
}

// Color a partir del texto guardado en Room ("Prospecto", "Cerrado", ...).
fun colorEtapa(etapa: String): Color = EtapaVenta.desdeTexto(etapa).color()

// Etiqueta de etapa: muestra texto + color (accesibilidad: no depende solo del color).
@Composable
fun EtapaChip(etapa: String, modifier: Modifier = Modifier) {
    Text(
        text = etapa,
        color = Color.White,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .background(colorEtapa(etapa), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}
