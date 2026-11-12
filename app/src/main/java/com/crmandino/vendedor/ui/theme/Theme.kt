package com.crmandino.vendedor.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Material 3 con los colores de marca y formas redondeadas (estilo "vidrio" tipo iOS).
private val CrmColorScheme = lightColorScheme(
    primary = AzulAndino,
    onPrimary = Superficie,
    primaryContainer = NieveAndina,
    onPrimaryContainer = AzulAndino,
    secondary = TurquesaLago,
    onSecondary = Superficie,
    secondaryContainer = TurquesaLago.copy(alpha = 0.16f),
    onSecondaryContainer = AzulAndino,
    tertiary = DoradoInca,
    onTertiary = TextoPrincipal,
    background = Color.Transparent,   // el fondo lo pinta FondoAndino (luces + degradado)
    onBackground = TextoPrincipal,
    surface = Superficie,
    onSurface = TextoPrincipal,
    surfaceVariant = NieveAndina,
    onSurfaceVariant = TextoSecundario,
    error = RojoError,
    onError = Superficie
)

private val FormasVidrio = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun CRMAndinoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CrmColorScheme,
        typography = Typography,
        shapes = FormasVidrio,
        content = content
    )
}
