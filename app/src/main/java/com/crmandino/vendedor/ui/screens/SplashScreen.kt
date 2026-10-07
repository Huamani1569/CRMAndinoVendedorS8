package com.crmandino.vendedor.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crmandino.vendedor.ui.components.AppLogo
import com.crmandino.vendedor.ui.theme.AzulAndino
import com.crmandino.vendedor.ui.theme.AzulAndinoOscuro
import com.crmandino.vendedor.ui.theme.DoradoInca
import com.crmandino.vendedor.ui.theme.NieveAndina
import kotlinx.coroutines.delay

// Splash Screen: muestra la identidad visual y, tras una carga breve, pasa al Login.
@Composable
fun SplashScreen(onFinalizar: () -> Unit) {
    val escala = remember { Animatable(0.6f) }
    val opacidad = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        opacidad.animateTo(1f, animationSpec = tween(700))
        escala.animateTo(1f, animationSpec = tween(500))
        delay(1600)
        onFinalizar()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(AzulAndino, AzulAndinoOscuro)))
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.alpha(opacidad.value)
        ) {
            AppLogo(size = 140.dp, modifier = Modifier.scale(escala.value))
            Spacer(Modifier.height(24.dp))
            Text(
                "CRM Andino Perú",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Tu embudo de ventas, siempre en orden",
                style = MaterialTheme.typography.bodyLarge,
                color = NieveAndina
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = DoradoInca,
                strokeWidth = 3.dp,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Cargando...",
                style = MaterialTheme.typography.labelMedium,
                color = NieveAndina,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
