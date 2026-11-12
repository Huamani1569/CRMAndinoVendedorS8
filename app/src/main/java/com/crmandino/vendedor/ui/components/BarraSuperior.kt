package com.crmandino.vendedor.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable

// Barra superior de las pantallas que vienen del parcial: ahora usa el estilo de vidrio (BarraVidrio).
@Composable
fun BarraSuperior(
    titulo: String,
    subtitulo: String? = null,
    onAtras: (() -> Unit)? = null,
    acciones: @Composable RowScope.() -> Unit = {}
) = BarraVidrio(titulo, subtitulo, onAtras, acciones)
