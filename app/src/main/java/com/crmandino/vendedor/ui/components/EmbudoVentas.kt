package com.crmandino.vendedor.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crmandino.vendedor.model.ResumenEtapa

/**
 * EMBUDO DE VENTAS dibujado con Canvas: un trapecio por etapa, cada uno más angosto que el anterior.
 * El texto encima indica la etapa, la cantidad de leads y el valor potencial acumulado.
 */
@Composable
fun EmbudoVentas(resumen: List<ResumenEtapa>, onElegirEtapa: (String) -> Unit = {}) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val total = resumen.size.coerceAtLeast(1)
            resumen.forEachIndexed { indice, fila ->
                // Ancho superior e inferior del trapecio (en fracción del ancho disponible)
                val anchoArriba = 1f - indice * (0.55f / total)
                val anchoAbajo = 1f - (indice + 1) * (0.55f / total)
                val color = colorEtapa(fila.etapa)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clickable { onElegirEtapa(fila.etapa) },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(Modifier.matchParentSize()) {
                        val w = size.width
                        val h = size.height
                        val trapecio = Path().apply {
                            moveTo((w - w * anchoArriba) / 2f, 0f)
                            lineTo((w + w * anchoArriba) / 2f, 0f)
                            lineTo((w + w * anchoAbajo) / 2f, h)
                            lineTo((w - w * anchoAbajo) / 2f, h)
                            close()
                        }
                        drawPath(trapecio, color = color)
                        // Pequeño brillo superior para dar volumen
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.15f),
                            topLeft = Offset((w - w * anchoArriba) / 2f + 6f, 3f),
                            size = androidx.compose.ui.geometry.Size(w * anchoArriba - 12f, h * 0.25f),
                            cornerRadius = CornerRadius(8f, 8f)
                        )
                    }
                    Text(
                        "${fila.etapa} · ${fila.cantidad} · ${formatearSoles(fila.valor)}",
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
