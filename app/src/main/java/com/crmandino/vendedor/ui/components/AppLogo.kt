package com.crmandino.vendedor.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.crmandino.vendedor.ui.theme.AzulAndino
import com.crmandino.vendedor.ui.theme.DoradoInca
import com.crmandino.vendedor.ui.theme.NieveAndina
import com.crmandino.vendedor.ui.theme.TurquesaLago

/**
 * Logo de CRM Andino Perú dibujado con Canvas:
 * dos montañas andinas (la región) + una línea dorada ascendente (crecimiento de ventas).
 * Se dibuja sobre una cuadrícula de 108x108 y se escala al tamaño pedido.
 */
@Composable
fun AppLogo(modifier: Modifier = Modifier, size: Dp = 96.dp) {
    Canvas(modifier = modifier.size(size)) {
        val u = this.size.width / 108f
        fun p(x: Float, y: Float) = Offset(x * u, y * u)

        // Círculo de fondo
        drawCircle(color = AzulAndino)

        // Montaña posterior
        drawPath(Path().apply {
            moveTo(p(22f, 80f).x, p(22f, 80f).y)
            lineTo(p(48f, 37f).x, p(48f, 37f).y)
            lineTo(p(74f, 80f).x, p(74f, 80f).y)
            close()
        }, color = TurquesaLago)

        // Montaña frontal
        drawPath(Path().apply {
            moveTo(p(43f, 80f).x, p(43f, 80f).y)
            lineTo(p(66f, 49f).x, p(66f, 49f).y)
            lineTo(p(89f, 80f).x, p(89f, 80f).y)
            close()
        }, color = NieveAndina)

        // Línea de crecimiento de ventas
        drawPath(Path().apply {
            moveTo(p(24f, 68f).x, p(24f, 68f).y)
            lineTo(p(43f, 58f).x, p(43f, 58f).y)
            lineTo(p(56f, 65f).x, p(56f, 65f).y)
            lineTo(p(77f, 41f).x, p(77f, 41f).y)
        }, color = DoradoInca, style = Stroke(width = 6f * u, cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Punta de flecha
        drawPath(Path().apply {
            moveTo(p(85f, 32f).x, p(85f, 32f).y)
            lineTo(p(70f, 38f).x, p(70f, 38f).y)
            lineTo(p(80f, 48f).x, p(80f, 48f).y)
            close()
        }, color = DoradoInca)
    }
}
