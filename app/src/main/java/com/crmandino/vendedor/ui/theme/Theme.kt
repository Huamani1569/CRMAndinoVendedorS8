package com.crmandino.vendedor.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Se usa un solo esquema claro con los colores de marca
// (sin colores dinámicos) para mantener la identidad visual.
private val CrmColorScheme = lightColorScheme(
    primary = AzulAndino,
    onPrimary = Superficie,
    secondary = TurquesaLago,
    onSecondary = Superficie,
    tertiary = DoradoInca,
    onTertiary = TextoPrincipal,
    background = FondoClaro,
    onBackground = TextoPrincipal,
    surface = Superficie,
    onSurface = TextoPrincipal,
    onSurfaceVariant = TextoSecundario,
    error = RojoError,
    onError = Superficie
)

@Composable
fun CRMAndinoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CrmColorScheme,
        typography = Typography,
        content = content
    )
}
