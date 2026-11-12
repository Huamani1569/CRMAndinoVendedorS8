package com.crmandino.vendedor.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val FormasVidrio = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

// Material 3 con los colores de marca, modo CLARO y OSCURO, y formas redondeadas (estilo "vidrio").
@Composable
fun CRMAndinoTheme(content: @Composable () -> Unit) {
    val oscuro = when (Paleta.preferencia) {
        PreferenciaTema.SISTEMA -> isSystemInDarkTheme()
        PreferenciaTema.CLARO -> false
        PreferenciaTema.OSCURO -> true
    }
    if (Paleta.oscuro != oscuro) Paleta.oscuro = oscuro

    val esquema = if (oscuro) darkColorScheme(
        primary = AzulAndino, onPrimary = Color(0xFF0B1422),
        primaryContainer = NieveAndina, onPrimaryContainer = TextoPrincipal,
        secondary = TurquesaLago, onSecondary = Color(0xFF06201D),
        secondaryContainer = TurquesaLago.copy(alpha = 0.22f), onSecondaryContainer = TextoPrincipal,
        tertiary = DoradoInca, onTertiary = Color(0xFF2A1B00),
        background = Color.Transparent, onBackground = TextoPrincipal,
        surface = Superficie, onSurface = TextoPrincipal,
        surfaceVariant = NieveAndina, onSurfaceVariant = TextoSecundario,
        surfaceContainer = Superficie, surfaceContainerHigh = Color(0xFF212C3D), surfaceContainerHighest = Color(0xFF283548),
        error = RojoError, onError = Color(0xFF2B0703), outline = TextoSecundario
    ) else lightColorScheme(
        primary = AzulAndino, onPrimary = Color.White,
        primaryContainer = NieveAndina, onPrimaryContainer = AzulAndino,
        secondary = TurquesaLago, onSecondary = Color.White,
        secondaryContainer = TurquesaLago.copy(alpha = 0.16f), onSecondaryContainer = AzulAndino,
        tertiary = DoradoInca, onTertiary = TextoPrincipal,
        background = Color.Transparent,   // el fondo lo pinta FondoAndino (luces + degradado)
        onBackground = TextoPrincipal,
        surface = Superficie, onSurface = TextoPrincipal,
        surfaceVariant = NieveAndina, onSurfaceVariant = TextoSecundario,
        error = RojoError, onError = Color.White
    )

    MaterialTheme(colorScheme = esquema, typography = Typography, shapes = FormasVidrio, content = content)
}
