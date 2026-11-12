package com.crmandino.vendedor.util

import android.content.Context
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.crmandino.vendedor.ui.theme.Paleta
import com.crmandino.vendedor.ui.theme.PreferenciaTema

// Carga la preferencia de tema guardada (Sistema / Claro / Oscuro) y la guarda cuando el usuario la cambia.
fun ComponentActivity.prepararTema() {
    val prefs = getSharedPreferences("preferencias_ui", Context.MODE_PRIVATE)
    Paleta.preferencia = runCatching { PreferenciaTema.valueOf(prefs.getString("tema", null) ?: "SISTEMA") }
        .getOrDefault(PreferenciaTema.SISTEMA)
    Paleta.alCambiarPreferencia = { prefs.edit().putString("tema", it.name).apply() }
}

// Barras del sistema transparentes con íconos claros u oscuros según el tema actual.
@Composable
fun ComponentActivity.BarrasDelSistema() {
    val oscuro = Paleta.oscuro
    LaunchedEffect(oscuro) {
        val estilo = if (oscuro) SystemBarStyle.dark(Color.TRANSPARENT) else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = estilo, navigationBarStyle = estilo)
    }
}
