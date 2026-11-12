package com.crmandino.vendedor.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.crmandino.vendedor.model.EtapaVenta
import com.crmandino.vendedor.ui.components.BarraVidrio
import com.crmandino.vendedor.ui.components.BotonCapsula
import com.crmandino.vendedor.ui.components.CampoVidrio
import com.crmandino.vendedor.ui.components.ChipsVidrio
import com.crmandino.vendedor.ui.components.FondoAndino
import com.crmandino.vendedor.ui.components.LeadVidrio
import com.crmandino.vendedor.ui.components.SegmentadoVidrio
import com.crmandino.vendedor.ui.components.VacioVidrio
import com.crmandino.vendedor.ui.theme.TextoSecundario
import com.crmandino.vendedor.ui.theme.TurquesaLago
import com.crmandino.vendedor.viewmodel.LeadViewModel
import com.crmandino.vendedor.viewmodel.OrdenCartera

// RF-V01/V02: cartera del vendedor leída desde Room, filtrada por etapa y ordenada por puntaje (ML-01)
// o por días sin contacto. El color del círculo indica la probabilidad de cierre.
@Composable
fun ListScreen(
    leadViewModel: LeadViewModel,
    etapaFiltro: String?,
    onCambiarFiltro: (String?) -> Unit,
    onAbrirLead: (Int) -> Unit,
    onNuevoLead: () -> Unit,
    onAtras: () -> Unit
) {
    var orden by rememberSaveable { mutableStateOf(OrdenCartera.PUNTAJE) }
    var texto by rememberSaveable { mutableStateOf("") }
    val leads by remember(etapaFiltro, orden, texto) { leadViewModel.cartera(etapaFiltro, orden, texto) }
        .collectAsState(initial = emptyList())
    val todas = "Todas"
    val opciones = listOf(todas) + EtapaVenta.entries.map { it.etiqueta }

    FondoAndino {
        Column(Modifier.fillMaxSize()) {
            BarraVidrio("Mi cartera", "${leads.size} lead(s) · ordenados por ${orden.etiqueta.lowercase()}", onAtras = onAtras)
            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { CampoVidrio(texto, { texto = it }, "Buscar empresa, contacto o RUC", icono = Icons.Filled.Search) }
                item {
                    ChipsVidrio(
                        opciones,
                        etapaFiltro?.let { EtapaVenta.desdeTexto(it).etiqueta } ?: todas,
                        { elegido -> onCambiarFiltro(if (elegido == todas) null else EtapaVenta.entries.first { it.etiqueta == elegido }.name) }
                    )
                }
                item {
                    SegmentadoVidrio(OrdenCartera.entries.map { it.etiqueta }, orden.ordinal, { orden = OrdenCartera.entries[it] })
                }
                item {
                    Text(
                        "● verde ≥ 70 pts · ● ámbar 40–69 · ● gris < 40 (probabilidad de cierre calculada por el modelo ML-01)",
                        style = MaterialTheme.typography.labelMedium, color = TextoSecundario
                    )
                }
                if (leads.isEmpty()) {
                    item { VacioVidrio(Icons.Filled.PersonSearch, "No hay leads con este filtro", null) }
                } else {
                    items(leads, key = { it.id }) { lead -> LeadVidrio(lead, onClick = { onAbrirLead(lead.id) }) }
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
        Box(Modifier.align(Alignment.BottomEnd).navigationBarsPadding().padding(20.dp)) {
            BotonCapsula("Nuevo lead", onNuevoLead, icono = Icons.Filled.Add, color = TurquesaLago)
        }
    }
}
