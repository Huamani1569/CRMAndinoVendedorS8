package com.crmandino.vendedor.ui.screens

import android.graphics.drawable.GradientDrawable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.crmandino.vendedor.data.remote.dto.VisitaDto
import com.crmandino.vendedor.model.Lead
import com.crmandino.vendedor.ui.components.BarraVidrio
import com.crmandino.vendedor.ui.components.BotonCapsula
import com.crmandino.vendedor.ui.components.BotonCircular
import com.crmandino.vendedor.ui.components.Capsula
import com.crmandino.vendedor.ui.components.EtapaChip
import com.crmandino.vendedor.ui.components.FondoAndino
import com.crmandino.vendedor.ui.components.MensajeVidrio
import com.crmandino.vendedor.ui.components.PuntajeCirculo
import com.crmandino.vendedor.ui.components.TarjetaVidrio
import com.crmandino.vendedor.ui.components.VacioVidrio
import com.crmandino.vendedor.ui.components.colorPuntaje
import com.crmandino.vendedor.ui.theme.DoradoInca
import com.crmandino.vendedor.ui.theme.TextoSecundario
import com.crmandino.vendedor.ui.theme.TurquesaLago
import com.crmandino.vendedor.ui.theme.VerdeExito
import com.crmandino.vendedor.util.Formato
import com.crmandino.vendedor.viewmodel.LeadViewModel
import com.crmandino.vendedor.viewmodel.VentasViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.File

// RF-V11/V12/V13: RUTA DEL DÍA en un mapa (OpenStreetMap). Cada marcador tiene el color del puntaje
// (verde ≥ 70, ámbar 40–69, gris < 40). Desde la lista se hace el check-in con GPS.
@Composable
fun RutaScreen(
    ventasViewModel: VentasViewModel,
    leadViewModel: LeadViewModel,
    onAbrirLead: (Int) -> Unit,
    onAtras: () -> Unit
) {
    val ruta by ventasViewModel.ruta.collectAsState()
    val leads by leadViewModel.leads.collectAsState(initial = emptyList())
    val visitas = ruta?.datos.orEmpty()
    val porUuid = leads.associateBy { it.uuid }
    var seleccion by remember { mutableStateOf<String?>(null) }
    val iniciarCheckin = rememberCheckin(ventasViewModel)
    LaunchedEffect(Unit) { ventasViewModel.refrescarRuta() }

    FondoAndino {
        Column(Modifier.fillMaxSize()) {
            BarraVidrio(
                "Ruta del día",
                if (ruta?.sugerida == true) "Sugerida por puntaje · ${Formato.fecha(ruta?.fecha)}" else "Programada · ${Formato.fecha(ruta?.fecha)}",
                onAtras = onAtras,
                acciones = { BotonCircular(Icons.Filled.Refresh, "Actualizar ruta", { ventasViewModel.refrescarRuta() }) }
            )
            ventasViewModel.mensaje?.let {
                Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) { MensajeVidrio(it, ventasViewModel.mensajeEsError, ventasViewModel::cerrarMensaje) }
            }
            if (visitas.isEmpty()) {
                Box(Modifier.padding(16.dp)) {
                    VacioVidrio(Icons.Filled.Map, "No hay visitas para hoy", "Conéctate y actualiza: la ruta se sugiere con tus leads de mayor puntaje.")
                }
            } else {
                // Mapa
                Box(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    TarjetaVidrio(modifier = Modifier.height(300.dp), relleno = 4.dp) {
                        MapaRuta(visitas, seleccion, onSeleccionar = { seleccion = it }, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(22.dp)))
                    }
                }
                LazyColumn(
                    Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            "${visitas.count { it.checkinEn != null }} de ${visitas.size} visitadas · radio de check-in: ${ruta?.radioMaximoM ?: 300} m",
                            style = MaterialTheme.typography.labelMedium, color = TextoSecundario
                        )
                    }
                    itemsIndexed(visitas, key = { i, v -> (v.leadUuid ?: "") + i }) { i, v ->
                        val lead = v.leadUuid?.let { porUuid[it] }
                        ParadaRuta(i + 1, v, seleccionada = v.leadUuid == seleccion,
                            onAbrir = { lead?.let { onAbrirLead(it.id) }; seleccion = v.leadUuid },
                            onCheckin = lead?.let { l -> { iniciarCheckin(l) } })
                    }
                }
            }
        }
    }
}

@Composable
private fun ParadaRuta(numero: Int, v: VisitaDto, seleccionada: Boolean, onAbrir: () -> Unit, onCheckin: (() -> Unit)?) {
    val visitada = v.checkinEn != null
    TarjetaVidrio(onClick = onAbrir, relleno = 12.dp, tinte = if (seleccionada) TurquesaLago else if (visitada) VerdeExito else null) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("$numero", style = MaterialTheme.typography.titleLarge, color = colorPuntaje(v.puntaje))
            Spacer(Modifier.width(10.dp))
            PuntajeCirculo(v.puntaje, tamano = 40.dp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(v.empresa ?: "", style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(v.direccion ?: v.contacto ?: "", style = MaterialTheme.typography.bodyMedium, color = TextoSecundario, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    v.etapa?.let { EtapaChip(it) }
                    if (v.sugerida == true) Capsula("Sugerida", DoradoInca)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        if (visitada) {
            Capsula(
                "Check-in ${Formato.fechaHora(v.checkinEn)}" + (v.distanciaM?.let { " · a ${it.toInt()} m" } ?: ""),
                VerdeExito, icono = Icons.Filled.CheckCircle
            )
        } else if (onCheckin != null) {
            BotonCapsula("Hacer check-in aquí", onCheckin, Modifier.fillMaxWidth(), icono = Icons.Filled.GpsFixed, color = TurquesaLago)
        } else {
            Capsula("Sincroniza para ver este lead en tu celular", DoradoInca, icono = Icons.Filled.Schedule)
        }
    }
}

// Mapa OpenStreetMap (osmdroid) dentro de Compose: un marcador por parada, del color de su puntaje.
@Composable
private fun MapaRuta(visitas: List<VisitaDto>, seleccion: String?, onSeleccionar: (String?) -> Unit, modifier: Modifier = Modifier) {
    val puntos = visitas.mapNotNull { v ->
        val lat = v.latitudCliente
        val lon = v.longitudCliente
        if (lat != null && lon != null) v to GeoPoint(lat, lon) else null
    }
    var mapa by remember { mutableStateOf<MapView?>(null) }
    var encuadrado by remember { mutableStateOf(false) }
    DisposableEffect(Unit) { onDispose { mapa?.onDetach() } }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            Configuration.getInstance().apply {
                userAgentValue = ctx.packageName
                osmdroidBasePath = File(ctx.cacheDir, "osmdroid")
                osmdroidTileCache = File(ctx.cacheDir, "osmdroid/tiles")
            }
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(14.0)
                controller.setCenter(GeoPoint(-12.0651, -75.2049))   // Huancayo
                mapa = this
            }
        },
        update = { map ->
            map.overlays.clear()
            puntos.forEachIndexed { i, (v, punto) ->
                val color = if (v.checkinEn != null) VerdeExito else colorPuntaje(v.puntaje)
                val marcador = Marker(map).apply {
                    position = punto
                    title = "${i + 1}. ${v.empresa}"
                    snippet = "Puntaje ${v.puntaje ?: "–"} · ${v.direccion ?: ""}"
                    icon = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(color.toArgb())
                        setStroke(6, android.graphics.Color.WHITE)
                        val tam = if (v.leadUuid == seleccion) 70 else 52
                        setSize(tam, tam)
                    }
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    setOnMarkerClickListener { m, _ ->
                        m.showInfoWindow()
                        onSeleccionar(v.leadUuid)
                        true
                    }
                }
                map.overlays.add(marcador)
            }
            if (!encuadrado && puntos.size >= 2) {
                encuadrado = true
                val caja = BoundingBox.fromGeoPoints(puntos.map { it.second })
                map.post { runCatching { map.zoomToBoundingBox(caja.increaseByScale(1.4f), false) } }
            } else if (puntos.size == 1) {
                map.controller.setCenter(puntos[0].second)
            }
            map.invalidate()
        }
    )
}
