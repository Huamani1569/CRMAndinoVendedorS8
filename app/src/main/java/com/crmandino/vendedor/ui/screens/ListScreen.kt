package com.crmandino.vendedor.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.crmandino.vendedor.model.EtapaVenta
import com.crmandino.vendedor.ui.components.BarraSuperior
import com.crmandino.vendedor.ui.components.EstadoVacio
import com.crmandino.vendedor.ui.components.FiltroEtapas
import com.crmandino.vendedor.ui.components.LeadCard
import com.crmandino.vendedor.viewmodel.LeadViewModel

// RF06 + RF07: listado de leads leídos desde Room, con filtro por etapa.
@Composable
fun ListScreen(
    leadViewModel: LeadViewModel,
    etapaFiltro: String?,
    onCambiarFiltro: (String?) -> Unit,
    onAbrirLead: (Int) -> Unit,
    onNuevoLead: () -> Unit,
    onAtras: () -> Unit
) {
    val leads by remember(etapaFiltro) { leadViewModel.leadsPorEtapa(etapaFiltro) }
        .collectAsState(initial = emptyList())

    Scaffold(
        topBar = { BarraSuperior(titulo = "Leads", subtitulo = "${leads.size} registros", onAtras = onAtras) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNuevoLead, containerColor = MaterialTheme.colorScheme.secondary) {
                Icon(Icons.Filled.Add, contentDescription = "Nuevo lead")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                FiltroEtapas(
                    etapas = EtapaVenta.etiquetas,
                    seleccionada = etapaFiltro,
                    onSeleccionar = onCambiarFiltro
                )
            }
            if (leads.isEmpty()) {
                item { EstadoVacio("No hay leads en esta etapa") }
            } else {
                items(leads, key = { it.id }) { lead ->
                    LeadCard(lead, onClick = { onAbrirLead(lead.id) })
                }
            }
            item { Spacer(Modifier.height(64.dp)) } // Para que el botón + no tape el último lead
        }
    }
}
