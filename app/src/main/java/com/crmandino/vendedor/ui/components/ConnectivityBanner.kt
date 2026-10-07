package com.crmandino.vendedor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crmandino.vendedor.ui.theme.RojoError

// Estado de conexión que comparten todas las pantallas (lo provee AppNavigation).
data class EstadoConexionUi(val conectado: Boolean = true, val pendientes: Int = 0, val errores: Int = 0)

val LocalEstadoConexion = compositionLocalOf { EstadoConexionUi() }

// Franja roja debajo de la barra superior cuando NO hay conexión.
@Composable
fun ConnectivityBanner() {
    val estado = LocalEstadoConexion.current
    if (estado.conectado) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RojoError)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.CloudOff, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Sin conexión · datos locales · ${estado.pendientes} cambio(s) pendiente(s)",
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}
