package com.crmandino.vendedor.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.crmandino.vendedor.data.remote.dto.CotizacionDto
import com.crmandino.vendedor.data.remote.dto.HistorialDto
import com.crmandino.vendedor.model.EstadoSincronizacion
import com.crmandino.vendedor.model.EtapaVenta
import com.crmandino.vendedor.model.Interaccion
import com.crmandino.vendedor.model.Lead
import com.crmandino.vendedor.model.Tarea
import com.crmandino.vendedor.ui.components.AccionIcono
import com.crmandino.vendedor.ui.components.BarraVidrio
import com.crmandino.vendedor.ui.components.BotonCircular
import com.crmandino.vendedor.ui.components.CampoVidrio
import com.crmandino.vendedor.ui.components.Capsula
import com.crmandino.vendedor.ui.components.ChipsVidrio
import com.crmandino.vendedor.ui.components.EstadoSyncChip
import com.crmandino.vendedor.ui.components.EtapaChip
import com.crmandino.vendedor.ui.components.FilaDato
import com.crmandino.vendedor.ui.components.FondoAndino
import com.crmandino.vendedor.ui.components.IconoCirculo
import com.crmandino.vendedor.ui.components.KpiVidrio
import com.crmandino.vendedor.ui.components.MensajeVidrio
import com.crmandino.vendedor.ui.components.PuntajeCirculo
import com.crmandino.vendedor.ui.components.Seccion
import com.crmandino.vendedor.ui.components.TarjetaVidrio
import com.crmandino.vendedor.ui.components.VacioVidrio
import com.crmandino.vendedor.ui.components.colorPuntaje
import com.crmandino.vendedor.ui.components.fechaDentroDe
import com.crmandino.vendedor.ui.theme.AzulAndino
import com.crmandino.vendedor.ui.theme.DoradoInca
import com.crmandino.vendedor.ui.theme.RojoError
import com.crmandino.vendedor.ui.theme.TextoSecundario
import com.crmandino.vendedor.ui.theme.TurquesaLago
import com.crmandino.vendedor.ui.theme.VerdeExito
import com.crmandino.vendedor.util.Formato
import com.crmandino.vendedor.viewmodel.InteraccionViewModel
import com.crmandino.vendedor.viewmodel.LeadViewModel
import com.crmandino.vendedor.viewmodel.RESULTADOS_CONTACTO
import com.crmandino.vendedor.viewmodel.TareaViewModel
import com.crmandino.vendedor.viewmodel.VentasViewModel
import kotlinx.coroutines.flow.flowOf

// Detalle del lead: puntaje y su explicación (ML-01), contacto en un toque (llamar / WhatsApp),
// cotizar, check-in GPS, cambio de etapa, interacciones con resultado, tareas e historial.
@Composable
fun DetailScreen(
    leadId: Int,
    leadViewModel: LeadViewModel,
    interaccionViewModel: InteraccionViewModel,
    tareaViewModel: TareaViewModel,
    ventasViewModel: VentasViewModel,
    onEditar: () -> Unit,
    onCotizar: (Int) -> Unit,
    onFichaCliente: (Int) -> Unit,
    onAtras: () -> Unit
) {
    val context = LocalContext.current
    val lead by remember(leadId) { leadViewModel.leadPorId(leadId) }.collectAsState(initial = null)
    val leadUuid = lead?.uuid
    val interacciones by remember(leadUuid) {
        if (leadUuid == null) flowOf(emptyList<Interaccion>()) else interaccionViewModel.interaccionesDe(leadUuid)
    }.collectAsState(initial = emptyList())
    val tareas by remember(leadUuid) {
        if (leadUuid == null) flowOf(emptyList<Tarea>()) else tareaViewModel.tareasDe(leadUuid)
    }.collectAsState(initial = emptyList())
    val cotizaciones by ventasViewModel.cotizaciones.collectAsState()
    val remoto = lead?.remoteId
    val historial by remember(remoto) { if (remoto == null) flowOf(emptyList<HistorialDto>()) else ventasViewModel.historial(remoto) }
        .collectAsState(initial = emptyList())
    LaunchedEffect(remoto) { if (remoto != null) ventasViewModel.refrescarHistorial(remoto) }

    var interaccionTipo by remember { mutableStateOf<String?>(null) }      // abre el diálogo con ese tipo
    var interaccionEditando by remember { mutableStateOf<Interaccion?>(null) }
    var mostrarEtapa by remember { mutableStateOf(false) }
    val iniciarCheckin = rememberCheckin(ventasViewModel)

    fun abrir(intent: Intent) {
        try { context.startActivity(intent) } catch (e: ActivityNotFoundException) { ventasViewModel.avisar("No hay una app para esta acción en el celular.", true) }
    }

    FondoAndino {
        Column(Modifier.fillMaxSize()) {
            BarraVidrio(
                titulo = lead?.empresa ?: "Lead",
                subtitulo = lead?.let { "${it.etapaVenta.etiqueta} · ${it.vendedorNombre}" },
                onAtras = onAtras,
                acciones = { BotonCircular(Icons.Filled.Edit, "Editar lead", onEditar) }
            )
            val actual = lead
            if (actual == null) {
                Box(Modifier.padding(16.dp)) { VacioVidrio(Icons.Filled.Business, "Cargando lead…", "Si no aparece, ya no está asignado a ti.") }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ventasViewModel.mensaje?.let { m -> item { MensajeVidrio(m, ventasViewModel.mensajeEsError, ventasViewModel::cerrarMensaje) } }
                    item { CabeceraLead(actual) }

                    // Acciones de un toque
                    item {
                        val tel = actual.telefono
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            KpiVidrio(Icons.Filled.Call, "Llamar", tel ?: "sin celular", AzulAndino, Modifier.weight(1f), onClick = {
                                if (tel == null) ventasViewModel.avisar("Registra el celular del lead para llamar.", true)
                                else { abrir(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$tel"))); interaccionEditando = null; interaccionTipo = "Llamada" }
                            })
                            KpiVidrio(Icons.Filled.Chat, "WhatsApp", "mensaje", VerdeExito, Modifier.weight(1f), onClick = {
                                if (tel == null) ventasViewModel.avisar("Registra el celular del lead para escribirle.", true)
                                else { abrir(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/51$tel"))); interaccionEditando = null; interaccionTipo = "WhatsApp" }
                            })
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            KpiVidrio(Icons.Filled.ReceiptLong, "Cotizar", "catálogo", TurquesaLago, Modifier.weight(1f), onClick = { onCotizar(actual.id) })
                            KpiVidrio(Icons.Filled.GpsFixed, "Check-in", "visita GPS", DoradoInca, Modifier.weight(1f), onClick = { iniciarCheckin(actual) })
                            KpiVidrio(Icons.Filled.SwapHoriz, "Etapa", actual.etapaVenta.etiqueta, AzulAndino, Modifier.weight(1f), onClick = {
                                leadViewModel.limpiarErrores(); mostrarEtapa = true
                            })
                        }
                    }
                    actual.clienteId?.let { idCliente ->
                        item {
                            TarjetaVidrio(onClick = { onFichaCliente(idCliente) }, tinte = TurquesaLago) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconoCirculo(Icons.Filled.Business, TurquesaLago)
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text("Ficha del cliente", style = MaterialTheme.typography.titleMedium)
                                        Text("Historial de compras y productos recomendados (ML)", style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
                                    }
                                }
                            }
                        }
                    }

                    // Cotizaciones de este lead
                    val propias = cotizaciones.filter { it.leadUuid == actual.uuid }
                    if (propias.isNotEmpty()) {
                        item { Seccion("Cotizaciones (${propias.size})") }
                        items(propias, key = { "c" + it.id }) { CotizacionFila(it) }
                    }

                    // Interacciones
                    item {
                        Seccion("Interacciones (${interacciones.size})", accion = {
                            TextButton(onClick = { interaccionViewModel.limpiarError(); interaccionEditando = null; interaccionTipo = "Reunión" }) { Text("+ Registrar") }
                        })
                    }
                    if (interacciones.isEmpty()) {
                        item { VacioVidrio(Icons.Filled.Groups, "Aún no hay interacciones", "Llama, escribe o visita y registra el resultado.") }
                    } else {
                        items(interacciones, key = { "i" + it.uuid }) { inter ->
                            InteraccionItem(inter,
                                onEditar = { interaccionViewModel.limpiarError(); interaccionEditando = inter; interaccionTipo = inter.tipo },
                                onEliminar = { interaccionViewModel.eliminar(inter) })
                        }
                    }

                    // Tareas
                    item { Seccion("Tareas (${tareas.size})") }
                    if (tareas.isEmpty()) {
                        item { VacioVidrio(Icons.Filled.Add, "Sin tareas", "Al cambiar de etapa puedes crear un recordatorio.") }
                    } else {
                        items(tareas, key = { "t" + it.uuid }) { t ->
                            TareaItem(t, onCambiarEstado = { tareaViewModel.cambiarEstado(t) }, onEliminar = { tareaViewModel.eliminar(t) })
                        }
                    }

                    // Historial (RNF-09)
                    if (historial.isNotEmpty()) {
                        item {
                            TarjetaVidrio {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconoCirculo(Icons.Filled.History, AzulAndino, 34.dp)
                                    Spacer(Modifier.width(10.dp))
                                    Text("Historial del lead", style = MaterialTheme.typography.titleMedium)
                                }
                                historial.take(8).forEach { h ->
                                    val valorNuevo = if (h.campo == "ETAPA") EtapaVenta.desdeTexto(h.valorNuevo).etiqueta else h.valorNuevo
                                    val valorAnterior = if (h.campo == "ETAPA") EtapaVenta.desdeTexto(h.valorAnterior).etiqueta else h.valorAnterior
                                    Text(
                                        "${Formato.fechaHora(h.fecha)} · ${h.campo?.lowercase()}: ${valorAnterior ?: "—"} → ${valorNuevo ?: "—"} (${h.usuarioNombre})",
                                        style = MaterialTheme.typography.bodyMedium, color = TextoSecundario, modifier = Modifier.padding(top = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                    item { DatosSincronizacion(actual) }
                }
            }
        }
    }

    val actual = lead
    val tipo = interaccionTipo
    if (actual != null && tipo != null) {
        InteraccionDialog(
            inicial = interaccionEditando,
            tipoInicial = tipo,
            tipos = interaccionViewModel.tipos,
            error = interaccionViewModel.error,
            onGuardar = { t, fecha, resultado, nota ->
                interaccionViewModel.guardar(interaccionEditando?.id ?: 0, actual.uuid, t, fecha, resultado, nota, onListo = { interaccionTipo = null })
            },
            onCancelar = { interaccionTipo = null }
        )
    }
    if (actual != null && mostrarEtapa) {
        CambiarEtapaDialog(
            lead = actual,
            error = leadViewModel.errorEtapa,
            onGuardar = { etapa, motivo, recordatorio, fecha ->
                leadViewModel.cambiarEtapa(actual, etapa, motivo, recordatorio, fecha, onListo = { mostrarEtapa = false })
            },
            onCancelar = { mostrarEtapa = false }
        )
    }
}

@Composable
private fun CabeceraLead(lead: Lead) {
    TarjetaVidrio(tinte = colorPuntaje(lead.puntaje)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PuntajeCirculo(lead.puntaje, tamano = 64.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Probabilidad de cierre", style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
                Text(lead.puntaje?.let { "$it %" } ?: "Se calcula al sincronizar", style = MaterialTheme.typography.titleLarge)
                Text(Formato.soles(lead.valorPotencial), style = MaterialTheme.typography.titleMedium, color = AzulAndino, fontWeight = FontWeight.Bold)
            }
            EtapaChip(lead.etapa)
        }
        if (lead.factores.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text("¿Por qué este puntaje?", style = MaterialTheme.typography.titleMedium)
            lead.factores.forEach { f ->
                val sube = (f.peso ?: 0.0) >= 0
                Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Icon(
                        if (sube) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = if (sube) "Sube" else "Baja",
                        tint = if (sube) VerdeExito else RojoError
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(f.factor ?: "", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        FilaDato("Contacto", lead.contacto.ifBlank { "—" })
        lead.telefono?.let { FilaDato("Celular", it) }
        lead.correo?.let { FilaDato("Correo", it) }
        lead.ruc?.let { FilaDato("RUC", it) }
        lead.direccion?.let { FilaDato("Dirección", it) }
        if (lead.latitud != null) Capsula("Ubicación registrada", TurquesaLago, icono = Icons.Filled.Place)
        Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            when (lead.canalOrigen) {
                "PORTAL" -> Capsula("Vino del Portal${lead.codigoSeguimiento?.let { " · $it" } ?: ""}", TurquesaLago, icono = Icons.Filled.Language)
                "CAMPANA" -> Capsula("Respondió a una campaña", DoradoInca)
                else -> Capsula("Registrado por el vendedor", AzulAndino)
            }
        }
        lead.comentarioCliente?.let {
            Spacer(Modifier.height(6.dp))
            Text("Comentario del cliente: “$it”", style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
        }
        lead.motivoPerdida?.let { if (lead.etapaVenta == EtapaVenta.PERDIDO) Text("Motivo de pérdida: $it", color = RojoError) }
    }
}

@Composable
private fun CotizacionFila(c: CotizacionDto) {
    val (texto, color) = when (c.estado) {
        "PEND_APROBACION" -> "Por aprobar (supervisor)" to DoradoInca
        "ENVIADA" -> "Enviada · vence en ${c.diasParaVencer ?: "?"} día(s)" to TurquesaLago
        "ACEPTADA" -> "Aceptada" to VerdeExito
        "RECHAZADA" -> "Rechazada" to RojoError
        "BORRADOR" -> "Observada: revisa el descuento" to RojoError
        else -> (c.estado ?: "—") to TextoSecundario
    }
    TarjetaVidrio(relleno = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconoCirculo(Icons.Filled.ReceiptLong, color, 36.dp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(c.numero ?: "", style = MaterialTheme.typography.titleMedium)
                Capsula(texto, color)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(Formato.soles(c.total), fontWeight = FontWeight.Bold, color = AzulAndino)
                if ((c.descuentoPct ?: 0.0) > 0) Text("−${c.descuentoPct} %", style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
            }
        }
        c.motivoRechazo?.let { Text("Motivo: ${it.lowercase()}${c.comentarioRechazo?.let { x -> " · $x" } ?: ""}", style = MaterialTheme.typography.labelMedium, color = RojoError) }
    }
}

@Composable
private fun DatosSincronizacion(lead: Lead) {
    TarjetaVidrio(relleno = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Sincronización", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            EstadoSyncChip(lead.estadoSync)
        }
        Spacer(Modifier.height(6.dp))
        val estilo = MaterialTheme.typography.bodySmall
        Text("ID local (Room): ${lead.id} · ID remoto: ${lead.remoteId ?: "aún no asignado"}", style = estilo, color = TextoSecundario)
        Text("UUID: ${lead.uuid}", style = estilo, color = TextoSecundario)
        Text("Versión confirmada por el servidor: ${lead.version}", style = estilo, color = TextoSecundario)
        lead.mensajeError?.let { Text("Motivo: $it", style = estilo, color = RojoError, fontWeight = FontWeight.Medium) }
    }
}

private fun iconoTipo(tipo: String): ImageVector = when (tipo) {
    "Llamada" -> Icons.Filled.Call
    "WhatsApp" -> Icons.Filled.Chat
    "Correo" -> Icons.Filled.Email
    "Visita" -> Icons.Filled.Place
    else -> Icons.Filled.Groups
}

@Composable
private fun InteraccionItem(interaccion: Interaccion, onEditar: () -> Unit, onEliminar: () -> Unit) {
    TarjetaVidrio(relleno = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconoCirculo(iconoTipo(interaccion.tipo), AzulAndino, 36.dp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("${interaccion.tipo} · ${interaccion.fecha}", style = MaterialTheme.typography.titleMedium)
                interaccion.resultado?.let {
                    Capsula(RESULTADOS_CONTACTO[it] ?: it, if (it == "INTERESADO" || it == "CONTESTO") VerdeExito else DoradoInca)
                }
                if (interaccion.nota.isNotBlank()) {
                    Text(interaccion.nota, style = MaterialTheme.typography.bodyMedium, color = TextoSecundario, maxLines = 3, overflow = TextOverflow.Ellipsis)
                }
                if (interaccion.estadoSync != EstadoSincronizacion.SINCRONIZADO) EstadoSyncChip(interaccion.estadoSync, Modifier.padding(top = 4.dp))
            }
            AccionIcono(Icons.Filled.Edit, "Editar", onEditar)
            AccionIcono(Icons.Filled.Delete, "Eliminar", onEliminar, RojoError)
        }
    }
}

@Composable
private fun TareaItem(tarea: Tarea, onCambiarEstado: () -> Unit, onEliminar: () -> Unit) {
    TarjetaVidrio(relleno = 8.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = tarea.completada, onCheckedChange = { onCambiarEstado() })
            Column(Modifier.weight(1f)) {
                Text(tarea.descripcion, style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (tarea.completada) TextDecoration.LineThrough else null)
                Text(if (tarea.completada) "Completada" else "Vence ${tarea.fechaLimite}", style = MaterialTheme.typography.labelMedium,
                    color = if (tarea.completada) TurquesaLago else DoradoInca)
                if (tarea.estadoSync != EstadoSincronizacion.SINCRONIZADO) EstadoSyncChip(tarea.estadoSync, Modifier.padding(top = 4.dp))
            }
            AccionIcono(Icons.Filled.Delete, "Eliminar tarea", onEliminar, RojoError)
        }
    }
}

// RF-V05/V06: registrar o editar una interacción con su resultado.
@Composable
private fun InteraccionDialog(
    inicial: Interaccion?,
    tipoInicial: String,
    tipos: List<String>,
    error: String?,
    onGuardar: (tipo: String, fecha: String, resultado: String?, nota: String) -> Unit,
    onCancelar: () -> Unit
) {
    var tipo by remember { mutableStateOf(inicial?.tipo ?: tipoInicial) }
    var fecha by remember { mutableStateOf(inicial?.fecha ?: fechaDentroDe(0)) }
    var resultado by remember { mutableStateOf(inicial?.resultado) }
    var nota by remember { mutableStateOf(inicial?.nota ?: "") }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(if (inicial == null) "¿Cómo te fue?" else "Editar interacción") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ChipsVidrio(tipos, tipo, { tipo = it })
                Text("Resultado", style = MaterialTheme.typography.labelMedium)
                ChipsVidrio(RESULTADOS_CONTACTO.values.toList(), resultado?.let { RESULTADOS_CONTACTO[it] }, { texto ->
                    resultado = RESULTADOS_CONTACTO.entries.first { it.value == texto }.key
                })
                CampoVidrio(fecha, { fecha = it }, "Fecha (dd/mm/aaaa)")
                CampoVidrio(nota, { nota = it }, if (tipo == "Reunión") "Nota (obligatoria)" else "Nota (opcional)", lineas = 2)
                if (error != null) Text(error, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = { TextButton(onClick = { onGuardar(tipo, fecha, resultado, nota) }) { Text("Guardar", fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}

// Cambiar la etapa con las transiciones válidas; si se pierde, pide el motivo.
@Composable
private fun CambiarEtapaDialog(
    lead: Lead,
    error: String?,
    onGuardar: (etapa: EtapaVenta, motivo: String, recordatorio: String, fecha: String) -> Unit,
    onCancelar: () -> Unit
) {
    val opciones = lead.etapaVenta.siguientes()
    var etapa by remember { mutableStateOf(opciones.firstOrNull()) }
    var motivo by remember { mutableStateOf("") }
    var recordatorio by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf(fechaDentroDe(3)) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Cambiar etapa") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (opciones.isEmpty()) {
                    Text("Un lead GANADO ya no cambia de etapa.")
                } else {
                    Text("Ahora: ${lead.etapaVenta.etiqueta}. Pasar a:", style = MaterialTheme.typography.labelMedium)
                    ChipsVidrio(opciones.map { it.etiqueta }, etapa?.etiqueta, { t -> etapa = opciones.first { it.etiqueta == t } })
                    if (lead.etapaVenta != EtapaVenta.COTIZADO) {
                        Text("Para pasar a Cotizado, usa el botón Cotizar.", style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
                    }
                    if (etapa == EtapaVenta.PERDIDO) CampoVidrio(motivo, { motivo = it }, "Motivo de la pérdida (obligatorio)")
                    CampoVidrio(recordatorio, { recordatorio = it }, "Recordatorio de seguimiento (opcional)")
                    if (recordatorio.isNotBlank()) CampoVidrio(fecha, { fecha = it }, "Fecha límite (dd/mm/aaaa)")
                }
                if (error != null) Text(error, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            TextButton(onClick = { etapa?.let { onGuardar(it, motivo, recordatorio, fecha) } }, enabled = etapa != null) {
                Text("Guardar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}
