package com.crmandino.vendedor.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.crmandino.vendedor.data.remote.dto.CotizacionDto
import com.crmandino.vendedor.data.repository.ItemCarrito
import com.crmandino.vendedor.data.repository.VentasRepository
import com.crmandino.vendedor.ui.components.BarraVidrio
import com.crmandino.vendedor.ui.components.BotonCapsula
import com.crmandino.vendedor.ui.components.BotonCircular
import com.crmandino.vendedor.ui.components.CampoVidrio
import com.crmandino.vendedor.ui.components.Capsula
import com.crmandino.vendedor.ui.components.ChipsVidrio
import com.crmandino.vendedor.ui.components.FilaDato
import com.crmandino.vendedor.ui.components.FondoAndino
import com.crmandino.vendedor.ui.components.IconoCirculo
import com.crmandino.vendedor.ui.components.KpiVidrio
import com.crmandino.vendedor.ui.components.MensajeVidrio
import com.crmandino.vendedor.ui.components.PuntajeCirculo
import com.crmandino.vendedor.ui.components.Seccion
import com.crmandino.vendedor.ui.components.SegmentadoVidrio
import com.crmandino.vendedor.ui.components.TarjetaVidrio
import com.crmandino.vendedor.ui.components.VacioVidrio
import com.crmandino.vendedor.ui.theme.AzulAndino
import com.crmandino.vendedor.ui.theme.DoradoInca
import com.crmandino.vendedor.ui.theme.GrisNeutro
import com.crmandino.vendedor.ui.theme.RojoError
import com.crmandino.vendedor.ui.theme.TextoSecundario
import com.crmandino.vendedor.ui.theme.TurquesaLago
import com.crmandino.vendedor.ui.theme.VerdeExito
import com.crmandino.vendedor.util.Formato
import com.crmandino.vendedor.viewmodel.LeadViewModel
import com.crmandino.vendedor.viewmodel.VentasViewModel

// ======================================================================== NUEVA COTIZACIÓN
// RF-V09/V10: el vendedor arma la cotización con el catálogo, aplica un descuento y la envía al Portal del cliente.
// Hasta 10 % la envía directamente; más de 10 % la aprueba el supervisor (RN-03). El total lo recalcula la base de datos.
@Composable
fun CotizacionFormScreen(leadId: Int, leadViewModel: LeadViewModel, ventasViewModel: VentasViewModel, onAtras: () -> Unit) {
    val lead by remember(leadId) { leadViewModel.leadPorId(leadId) }.collectAsState(initial = null)
    val productos by ventasViewModel.productos.collectAsState()
    val cantidades = remember { mutableStateMapOf<Int, Int>() }
    var descuento by rememberSaveable { mutableStateOf("0") }
    var categoria by rememberSaveable { mutableStateOf("Todas") }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) { if (productos.isEmpty()) ventasViewModel.refrescarCotizaciones() }

    val carrito = productos.filter { (cantidades[it.id] ?: 0) > 0 }.map { ItemCarrito(it, cantidades[it.id] ?: 0) }
    val subtotal = carrito.sumOf { it.subtotal }
    val pct = descuento.replace(",", ".").toDoubleOrNull() ?: 0.0
    val total = subtotal * (1 - pct / 100)
    val categorias = listOf("Todas") + productos.mapNotNull { it.categoria }.distinct()

    FondoAndino {
        Column(Modifier.fillMaxSize().imePadding()) {
            BarraVidrio("Nueva cotización", lead?.empresa, onAtras = onAtras)
            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                lead?.let { l ->
                    if (l.comentarioCliente != null) {
                        item { MensajeVidrio("El cliente pidió: ${l.comentarioCliente}") }
                    }
                }
                item { ChipsVidrio(categorias, categoria, { categoria = it }) }
                if (productos.isEmpty()) item { VacioVidrio(Icons.Filled.Inventory2, "Catálogo no descargado", "Sincroniza con conexión.") }
                items(productos.filter { categoria == "Todas" || it.categoria == categoria }, key = { it.id }) { p ->
                    val n = cantidades[p.id] ?: 0
                    TarjetaVidrio(relleno = 12.dp, tinte = if (n > 0) TurquesaLago else null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(p.nombre ?: "", style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text("${p.codigo ?: ""} · ${Formato.soles(p.precio)}", style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
                            }
                            IconButton(onClick = { cantidades[p.id] = (n - 1).coerceAtLeast(0) }, enabled = n > 0) {
                                Icon(Icons.Filled.RemoveCircleOutline, contentDescription = "Quitar", tint = if (n > 0) AzulAndino else GrisNeutro)
                            }
                            Text("$n", style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center, modifier = Modifier.widthIn(min = 28.dp))
                            IconButton(onClick = { cantidades[p.id] = n + 1 }) {
                                Icon(Icons.Filled.AddCircle, contentDescription = "Agregar", tint = TurquesaLago)
                            }
                        }
                    }
                }
            }
            // Resumen y envío
            Box(Modifier.navigationBarsPadding().padding(16.dp)) {
                TarjetaVidrio {
                    CampoVidrio(descuento, { descuento = it.take(5); error = null }, "Descuento %", icono = Icons.Filled.Percent,
                        teclado = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                    if (pct > VentasRepository.DESCUENTO_MAXIMO) {
                        Spacer(Modifier.height(6.dp))
                        Capsula("Más de 10 %: la aprobará el supervisor antes de enviarla", DoradoInca, icono = Icons.Filled.Warning)
                    }
                    Spacer(Modifier.height(8.dp))
                    FilaDato("Subtotal (${carrito.sumOf { it.cantidad }} und.)", Formato.soles(subtotal))
                    FilaDato("Total", Formato.soles(total), destacado = true)
                    error?.let { Text(it, color = RojoError, style = MaterialTheme.typography.labelMedium) }
                    Spacer(Modifier.height(8.dp))
                    BotonCapsula(
                        if (pct > VentasRepository.DESCUENTO_MAXIMO) "Enviar a aprobación" else "Enviar al cliente", {
                            val l = lead
                            error = if (l == null) "Lead no encontrado" else ventasViewModel.crearCotizacion(l, carrito, descuento, onAtras)
                        },
                        Modifier.fillMaxWidth(), icono = Icons.Filled.Send, color = TurquesaLago
                    )
                }
            }
        }
    }
}

// ======================================================================== MIS COTIZACIONES
@Composable
fun CotizacionesScreen(ventasViewModel: VentasViewModel, onAtras: () -> Unit) {
    val lista by ventasViewModel.cotizaciones.collectAsState()
    var filtro by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { ventasViewModel.refrescarCotizaciones() }
    val grupos = listOf(
        "Abiertas" to lista.filter { it.estado in listOf("ENVIADA", "PEND_APROBACION", "BORRADOR") },
        "Aceptadas" to lista.filter { it.estado == "ACEPTADA" },
        "Otras" to lista.filter { it.estado in listOf("RECHAZADA", "VENCIDA") }
    )
    val ganado = lista.filter { it.estado == "ACEPTADA" }.sumOf { it.total ?: 0.0 }
    val respondidas = lista.count { it.estado == "ACEPTADA" || it.estado == "RECHAZADA" }

    FondoAndino {
        Column(Modifier.fillMaxSize()) {
            BarraVidrio("Mis cotizaciones", "${lista.size} en total", onAtras = onAtras,
                acciones = { BotonCircular(Icons.Filled.Refresh, "Actualizar", { ventasViewModel.refrescarCotizaciones() }) })
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        KpiVidrio(Icons.Filled.ReceiptLong, Formato.solesCorto(ganado), "Aceptado", VerdeExito, Modifier.weight(1f))
                        KpiVidrio(
                            Icons.AutoMirrored.Filled.TrendingUp,
                            if (respondidas == 0) "—" else "${100 * lista.count { it.estado == "ACEPTADA" } / respondidas} %",
                            "Tasa de aceptación", TurquesaLago, Modifier.weight(1f)
                        )
                    }
                }
                item { SegmentadoVidrio(grupos.map { "${it.first} (${it.second.size})" }, filtro, { filtro = it }) }
                val visibles = grupos[filtro].second
                if (visibles.isEmpty()) item { VacioVidrio(Icons.Filled.ReceiptLong, "Sin cotizaciones aquí", null) }
                items(visibles, key = { it.id }) { CotizacionTarjeta(it) }
            }
        }
    }
}

@Composable
private fun CotizacionTarjeta(c: CotizacionDto) {
    val (texto, color) = when (c.estado) {
        "PEND_APROBACION" -> "Esperando aprobación" to DoradoInca
        "ENVIADA" -> "Enviada · vence en ${c.diasParaVencer ?: "?"} día(s)" to TurquesaLago
        "ACEPTADA" -> "Aceptada ${Formato.fecha(c.respondidaEn)}" to VerdeExito
        "RECHAZADA" -> "Rechazada" to RojoError
        "VENCIDA" -> "Vencida" to GrisNeutro
        "BORRADOR" -> "Observada por el supervisor" to RojoError
        else -> (c.estado ?: "—") to GrisNeutro
    }
    TarjetaVidrio {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconoCirculo(Icons.Filled.ReceiptLong, color)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(c.numero ?: "", style = MaterialTheme.typography.titleMedium)
                Text(c.empresa ?: "", style = MaterialTheme.typography.bodyMedium, color = TextoSecundario, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(Formato.soles(c.total), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AzulAndino)
        }
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Capsula(texto, color)
            if ((c.descuentoPct ?: 0.0) > 0) Capsula("−${c.descuentoPct} %", if ((c.descuentoPct ?: 0.0) > 10) DoradoInca else AzulAndino)
        }
        Text(c.items.orEmpty().joinToString { "${it.cantidad} × ${it.nombre}" }, style = MaterialTheme.typography.bodyMedium,
            color = TextoSecundario, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 6.dp))
        c.aprobadaPor?.let { Text("Aprobó: $it", style = MaterialTheme.typography.labelMedium, color = TextoSecundario) }
        c.motivoRechazo?.let { Text("Motivo del rechazo: ${it.lowercase()}${c.comentarioRechazo?.let { x -> " · $x" } ?: ""}", style = MaterialTheme.typography.labelMedium, color = RojoError) }
    }
}

// ======================================================================== FICHA DEL CLIENTE
// RF-V15/V16: historial de compras, valor del cliente (ML-03), riesgo (ML-02) y productos recomendados (ML-04).
@Composable
fun ClienteScreen(clienteId: Int, ventasViewModel: VentasViewModel, onAtras: () -> Unit) {
    val cliente by remember(clienteId) { ventasViewModel.cliente(clienteId) }.collectAsState(initial = null)
    val recomendaciones by remember(clienteId) { ventasViewModel.recomendaciones(clienteId) }.collectAsState(initial = emptyList())
    LaunchedEffect(clienteId) { ventasViewModel.refrescarCliente(clienteId) }

    FondoAndino {
        Column(Modifier.fillMaxSize()) {
            BarraVidrio(cliente?.razonSocial ?: "Cliente", cliente?.ruc?.let { "RUC $it" }, onAtras = onAtras)
            val c = cliente
            if (c == null) {
                Box(Modifier.padding(16.dp)) { VacioVidrio(Icons.Filled.Business, "Cargando ficha…", "Necesitas conexión la primera vez.") }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        TarjetaVidrio {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PuntajeCirculo(c.puntajeValor, tamano = 60.dp)
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Text("Valor del cliente (ML-03)", style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
                                    Text(if ((c.puntajeValor ?: 0) >= 70) "Cliente preferente" else "Cliente regular", style = MaterialTheme.typography.titleLarge)
                                    Text("CLTV estimado: ${Formato.soles(c.cltv, false)}", color = AzulAndino, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            FilaDato("Cliente desde", Formato.fecha(c.clienteDesde))
                            FilaDato("Compras", "${c.compras ?: 0} · ${Formato.soles(c.comprasTotal, false)}")
                            FilaDato("Última compra", Formato.fecha(c.ultimaCompra))
                            FilaDato("Tickets de servicio", "${c.ticketsPrevios ?: 0}" + (c.csatPromedio?.let { " · satisfacción $it ★" } ?: ""))
                            c.distrito?.let { FilaDato("Distrito", it) }
                        }
                    }
                    val prob = c.probCancelacion
                    if (prob != null) {
                        item {
                            val alto = prob >= 0.6
                            TarjetaVidrio(tinte = if (alto) RojoError else if (prob >= 0.3) DoradoInca else VerdeExito) {
                                Text("Riesgo de cancelación (ML-02): ${(prob * 100).toInt()} %", style = MaterialTheme.typography.titleMedium,
                                    color = if (alto) RojoError else AzulAndino)
                                c.factoresRiesgo.orEmpty().forEach { f ->
                                    Text("• ${f.factor}", style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
                                }
                                if (alto) Text("Visítalo pronto y ofrece una solución: es una cuenta en riesgo.", style = MaterialTheme.typography.labelMedium, color = RojoError)
                            }
                        }
                    }
                    item { Seccion("Recomendado para ofrecer (ML-04)") }
                    if (recomendaciones.isEmpty()) {
                        item { VacioVidrio(Icons.Filled.Lightbulb, "Sin recomendaciones por ahora", "Se calculan con lo que compran clientes parecidos.") }
                    }
                    items(recomendaciones, key = { "r" + it.productoId }) { r ->
                        TarjetaVidrio(tinte = TurquesaLago) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconoCirculo(Icons.Filled.Lightbulb, TurquesaLago)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(r.producto ?: "", style = MaterialTheme.typography.titleMedium)
                                    Text(r.razon ?: "", style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(Formato.soles(r.precio, false), fontWeight = FontWeight.Bold, color = AzulAndino)
                                    Capsula("${((r.confianza ?: 0.0) * 100).toInt()} % confianza", TurquesaLago)
                                }
                            }
                        }
                    }
                    val compras = c.historialCompras.orEmpty()
                    if (compras.isNotEmpty()) {
                        item { Seccion("Historial de compras") }
                        item {
                            TarjetaVidrio {
                                compras.take(15).forEachIndexed { i, x ->
                                    if (i > 0) HorizontalDivider(color = AzulAndino.copy(alpha = 0.08f))
                                    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                                        Column(Modifier.weight(1f)) {
                                            Text("${x.cantidad} × ${x.producto}", style = MaterialTheme.typography.bodyMedium)
                                            Text("${x.numero} · ${Formato.fecha(x.fecha)}", style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
                                        }
                                        Text(Formato.soles(x.monto, false), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ======================================================================== NOTIFICACIONES
@Composable
fun NotificacionesScreen(ventasViewModel: VentasViewModel, onAtras: () -> Unit) {
    val lista by ventasViewModel.notificaciones.collectAsState()
    LaunchedEffect(Unit) { ventasViewModel.marcarLeidas() }
    FondoAndino {
        Column(Modifier.fillMaxSize()) {
            BarraVidrio("Notificaciones", "${lista.size} aviso(s)", onAtras = onAtras)
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (lista.isEmpty()) item { VacioVidrio(Icons.Filled.Notifications, "No tienes notificaciones", null) }
                items(lista, key = { it.id }) { n ->
                    TarjetaVidrio(relleno = 14.dp) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconoCirculo(Icons.Filled.Notifications, if (n.leida == true) GrisNeutro else TurquesaLago, 36.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(n.titulo ?: "", style = MaterialTheme.typography.titleMedium, fontWeight = if (n.leida == true) FontWeight.Normal else FontWeight.Bold)
                                Text(n.cuerpo ?: "", style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
                                Text(Formato.haceCuanto(n.creadoEn), style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
                            }
                        }
                    }
                }
            }
        }
    }
}
