package com.crmandino.vendedor.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Fila de chips para elegir una opción (etapa, tipo de interacción, vendedor...).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorChips(
    opciones: List<String>,
    seleccionada: String?,
    onSeleccionar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(opciones) { opcion ->
            FilterChip(
                selected = opcion == seleccionada,
                onClick = { onSeleccionar(opcion) },
                label = { Text(opcion) }
            )
        }
    }
}

// Filtro por etapa del embudo con la opción "Todas" (null = sin filtro).
@Composable
fun FiltroEtapas(
    etapas: List<String>,
    seleccionada: String?,
    onSeleccionar: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val todas = "Todas"
    SelectorChips(
        opciones = listOf(todas) + etapas,
        seleccionada = seleccionada ?: todas,
        onSeleccionar = { onSeleccionar(if (it == todas) null else it) },
        modifier = modifier
    )
}
