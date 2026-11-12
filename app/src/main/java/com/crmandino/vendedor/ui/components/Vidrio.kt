package com.crmandino.vendedor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.crmandino.vendedor.ui.theme.AzulAndino
import com.crmandino.vendedor.ui.theme.BlancoVidrio
import com.crmandino.vendedor.ui.theme.Paleta
import com.crmandino.vendedor.ui.theme.PreferenciaTema
import com.crmandino.vendedor.ui.theme.SobreColor
import com.crmandino.vendedor.ui.theme.Superficie
import com.crmandino.vendedor.ui.theme.VidrioBorde
import com.crmandino.vendedor.ui.theme.DoradoInca
import com.crmandino.vendedor.ui.theme.FondoCieloAbajo
import com.crmandino.vendedor.ui.theme.FondoCieloArriba
import com.crmandino.vendedor.ui.theme.FondoCieloMedio
import com.crmandino.vendedor.ui.theme.GrisNeutro
import com.crmandino.vendedor.ui.theme.RojoError
import com.crmandino.vendedor.ui.theme.TextoPrincipal
import com.crmandino.vendedor.ui.theme.TextoSecundario
import com.crmandino.vendedor.ui.theme.TurquesaLago
import com.crmandino.vendedor.ui.theme.VerdeExito
import com.crmandino.vendedor.ui.theme.VidrioAlto
import com.crmandino.vendedor.ui.theme.VidrioBajo

/*
 * Componentes "Liquid Glass" (Material 3 + detalles de vidrio):
 * un fondo claro con luces de color difuminadas y, encima, tarjetas translúcidas
 * con borde de luz, esquinas muy redondeadas y botones en forma de cápsula.
 */

private val FormaTarjeta = RoundedCornerShape(26.dp)
private val FormaCapsula = RoundedCornerShape(50)

// Estado de conexión que comparten todas las pantallas (lo provee la navegación).
data class ConexionUi(val conectado: Boolean = true, val pendientes: Int = 0)

val LocalConexion = compositionLocalOf { ConexionUi() }

// Fondo de todas las pantallas: degradado suave + tres "luces" (turquesa, dorado y azul).
@Composable
fun FondoAndino(modifier: Modifier = Modifier, contenido: @Composable BoxScope.() -> Unit) {
    Box(
        modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(FondoCieloArriba, FondoCieloMedio, FondoCieloAbajo)))
            .drawBehind {
                val w = size.width
                val h = size.height
                fun luz(color: Color, x: Float, y: Float, radio: Float) = drawCircle(
                    brush = Brush.radialGradient(listOf(color, Color.Transparent), center = Offset(x, y), radius = radio),
                    radius = radio,
                    center = Offset(x, y)
                )
                luz(TurquesaLago.copy(alpha = 0.30f), w * 0.95f, h * 0.06f, w * 0.75f)
                luz(DoradoInca.copy(alpha = 0.22f), w * 0.05f, h * 0.45f, w * 0.70f)
                luz(AzulAndino.copy(alpha = 0.16f), w * 0.85f, h * 0.92f, w * 0.80f)
            },
        content = contenido
    )
}

// Tarjeta de vidrio: blanco translúcido, borde de luz y sombra suave.
@Composable
fun TarjetaVidrio(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    relleno: Dp = 16.dp,
    tinte: Color? = null,
    contenido: @Composable ColumnScope.() -> Unit
) {
    val arriba = tinte?.copy(alpha = 0.20f)?.compositeSobre(Superficie) ?: VidrioAlto
    Column(
        modifier
            .fillMaxWidth()
            .shadow(14.dp, FormaTarjeta, ambientColor = AzulAndino.copy(alpha = 0.10f), spotColor = AzulAndino.copy(alpha = 0.16f))
            .clip(FormaTarjeta)
            .background(Brush.verticalGradient(listOf(arriba, VidrioBajo)))
            .border(1.dp, Brush.linearGradient(listOf(VidrioBorde, VidrioBorde.copy(alpha = VidrioBorde.alpha * 0.25f))), FormaTarjeta)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(relleno),
        content = contenido
    )
}

// Mezcla un tinte suave sobre la superficie (blanca en modo claro, azul noche en modo oscuro).
private fun Color.compositeSobre(base: Color): Color = Color(
    red = base.red * (1 - alpha) + red * alpha,
    green = base.green * (1 - alpha) + green * alpha,
    blue = base.blue * (1 - alpha) + blue * alpha,
    alpha = 0.88f
)

// Cabecera grande tipo iOS: botón atrás en círculo de vidrio, título y acciones.
@Composable
fun BarraVidrio(
    titulo: String,
    subtitulo: String? = null,
    onAtras: (() -> Unit)? = null,
    acciones: @Composable RowScope.() -> Unit = {}
) {
    Column(Modifier.fillMaxWidth().statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(start = 12.dp, end = 8.dp, top = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onAtras != null) {
                BotonCircular(Icons.AutoMirrored.Filled.ArrowBack, "Volver", onAtras)
                Spacer(Modifier.width(8.dp))
            }
            Column(Modifier.weight(1f).padding(start = if (onAtras == null) 8.dp else 0.dp)) {
                Text(
                    titulo,
                    style = if (onAtras == null) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.titleLarge,
                    color = TextoPrincipal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitulo != null) {
                    Text(subtitulo, style = MaterialTheme.typography.bodyMedium, color = TextoSecundario, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                acciones()
                // En las pantallas principales: botón ☀/🌙 para elegir tema Sistema / Claro / Oscuro
                if (onAtras == null) BotonTema()
            }
        }
        AvisoSinConexion()
    }
}

// Cambia el tema: Automático (sistema) → Claro → Oscuro.
@Composable
fun BotonTema() {
    val (icono, texto) = when (Paleta.preferencia) {
        PreferenciaTema.SISTEMA -> Icons.Filled.BrightnessAuto to "Tema automático (toca para claro)"
        PreferenciaTema.CLARO -> Icons.Filled.LightMode to "Tema claro (toca para oscuro)"
        PreferenciaTema.OSCURO -> Icons.Filled.DarkMode to "Tema oscuro (toca para automático)"
    }
    BotonCircular(icono, texto, { Paleta.siguientePreferencia() })
}

// Botón redondo de vidrio (atrás, campana, sincronizar...). insignia > 0 muestra un contador.
@Composable
fun BotonCircular(icono: ImageVector, descripcion: String, onClick: () -> Unit, insignia: Int = 0) {
    Box(
        Modifier
            .padding(4.dp)
            .size(42.dp)
            .shadow(6.dp, CircleShape, ambientColor = AzulAndino.copy(alpha = 0.12f))
            .clip(CircleShape)
            .background(VidrioAlto)
            .border(1.dp, VidrioBorde, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (insignia > 0) {
            BadgedBox(badge = { Badge(containerColor = RojoError) { Text(if (insignia > 99) "99+" else "$insignia") } }) {
                Icon(icono, contentDescription = descripcion, tint = AzulAndino)
            }
        } else {
            Icon(icono, contentDescription = descripcion, tint = AzulAndino)
        }
    }
}

// Franja "Sin conexión" en forma de cápsula.
@Composable
fun AvisoSinConexion() {
    val estado = LocalConexion.current
    if (estado.conectado) return
    Row(
        Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(FormaCapsula)
            .background(RojoError.copy(alpha = 0.90f))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.CloudOff, contentDescription = null, tint = SobreColor, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            "Sin conexión · ${estado.pendientes} pendiente(s) por enviar",
            color = SobreColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// Botón principal en forma de cápsula, con indicador de carga.
@Composable
fun BotonCapsula(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null,
    color: Color = AzulAndino,
    cargando: Boolean = false,
    habilitado: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = habilitado && !cargando,
        shape = FormaCapsula,
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = SobreColor),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
        modifier = modifier.height(50.dp)
    ) {
        if (cargando) {
            CircularProgressIndicator(color = SobreColor, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
        } else {
            if (icono != null) {
                Icon(icono, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(texto, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

// Botón secundario: cápsula de vidrio con borde.
@Composable
fun BotonCapsulaSecundario(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null,
    color: Color = AzulAndino,
    habilitado: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = habilitado,
        shape = FormaCapsula,
        colors = ButtonDefaults.outlinedButtonColors(containerColor = VidrioAlto, contentColor = color),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        modifier = modifier.height(50.dp)
    ) {
        if (icono != null) {
            Icon(icono, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(texto, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// Etiqueta en cápsula (estado, etapa, prioridad...). Texto + color: no depende solo del color.
@Composable
fun Capsula(texto: String, color: Color, modifier: Modifier = Modifier, icono: ImageVector? = null) {
    Row(
        modifier
            .clip(FormaCapsula)
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, color.copy(alpha = 0.35f), FormaCapsula)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icono != null) {
            Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
        }
        Text(texto, color = color, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

// Selector segmentado tipo iOS (una cápsula con las opciones).
@Composable
fun SegmentadoVidrio(opciones: List<String>, seleccionada: Int, onSeleccionar: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(FormaCapsula)
            .background(BlancoVidrio.copy(alpha = 0.45f))
            .border(1.dp, VidrioBorde, FormaCapsula)
            .padding(4.dp)
    ) {
        opciones.forEachIndexed { i, texto ->
            val activo = i == seleccionada
            Box(
                Modifier
                    .weight(1f)
                    .clip(FormaCapsula)
                    .background(if (activo) BlancoVidrio else Color.Transparent)
                    .clickable { onSeleccionar(i) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    texto,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (activo) FontWeight.Bold else FontWeight.Medium,
                    color = if (activo) AzulAndino else TextoSecundario,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// Fila desplazable de cápsulas seleccionables (filtros, categorías, motivos...).
@Composable
fun ChipsVidrio(opciones: List<String>, seleccionada: String?, onSeleccionar: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        opciones.forEach { opcion ->
            val activo = opcion == seleccionada
            Text(
                opcion,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (activo) FontWeight.Bold else FontWeight.Medium,
                color = if (activo) SobreColor else AzulAndino,
                modifier = Modifier
                    .clip(FormaCapsula)
                    .background(if (activo) AzulAndino else BlancoVidrio.copy(alpha = 0.6f))
                    .border(1.dp, if (activo) AzulAndino else VidrioBorde, FormaCapsula)
                    .clickable { onSeleccionar(opcion) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }
    }
}

// Campo de texto con fondo de vidrio y bordes redondeados.
@Composable
fun CampoVidrio(
    valor: String,
    onValor: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    icono: ImageVector? = null,
    lineas: Int = 1,
    habilitado: Boolean = true,
    teclado: KeyboardOptions = KeyboardOptions.Default,
    transformacion: VisualTransformation = VisualTransformation.None,
    finalIcono: @Composable (() -> Unit)? = null,
    ayuda: String? = null
) {
    Column(modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = valor,
            onValueChange = onValor,
            label = { Text(etiqueta) },
            leadingIcon = icono?.let { { Icon(it, contentDescription = null) } },
            trailingIcon = finalIcono,
            isError = error != null,
            singleLine = lineas == 1,
            minLines = lineas,
            enabled = habilitado,
            keyboardOptions = teclado,
            visualTransformation = transformacion,
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = BlancoVidrio.copy(alpha = 0.85f),
                unfocusedContainerColor = BlancoVidrio.copy(alpha = 0.55f),
                disabledContainerColor = BlancoVidrio.copy(alpha = 0.35f),
                focusedBorderColor = TurquesaLago,
                unfocusedBorderColor = VidrioBorde
            ),
            modifier = Modifier.fillMaxWidth()
        )
        val nota = error ?: ayuda
        if (nota != null) {
            Text(
                nota,
                color = if (error != null) MaterialTheme.colorScheme.error else TextoSecundario,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(start = 14.dp, top = 2.dp)
            )
        }
    }
}

// Título de sección.
@Composable
fun Seccion(texto: String, modifier: Modifier = Modifier, accion: (@Composable () -> Unit)? = null) {
    Row(modifier.fillMaxWidth().padding(top = 6.dp, bottom = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(texto, style = MaterialTheme.typography.titleMedium, color = TextoPrincipal, modifier = Modifier.weight(1f))
        accion?.invoke()
    }
}

// Mensaje cuando una lista está vacía.
@Composable
fun VacioVidrio(icono: ImageVector, mensaje: String, detalle: String? = null) {
    TarjetaVidrio {
        Column(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icono, contentDescription = null, tint = TurquesaLago, modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(8.dp))
            Text(mensaje, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            if (detalle != null) {
                Text(detalle, style = MaterialTheme.typography.bodyMedium, color = TextoSecundario, textAlign = TextAlign.Center)
            }
        }
    }
}

// Indicador (KPI) en una tarjeta de vidrio.
@Composable
fun KpiVidrio(icono: ImageVector, valor: String, etiqueta: String, color: Color, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    TarjetaVidrio(modifier = modifier, relleno = 14.dp, onClick = onClick) {
        Box(
            Modifier.size(34.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) { Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(20.dp)) }
        Spacer(Modifier.height(8.dp))
        Text(valor, style = MaterialTheme.typography.titleLarge, color = TextoPrincipal, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(etiqueta, style = MaterialTheme.typography.labelMedium, color = TextoSecundario, maxLines = 2)
    }
}

// Color del puntaje ML: verde ≥ 70, ámbar 40–69, gris < 40 (o sin puntaje).
fun colorPuntaje(puntaje: Int?): Color = when {
    puntaje == null -> GrisNeutro
    puntaje >= 70 -> VerdeExito
    puntaje >= 40 -> DoradoInca
    else -> GrisNeutro
}

// Círculo con el puntaje (probabilidad de cierre o valor del cliente).
@Composable
fun PuntajeCirculo(puntaje: Int?, modifier: Modifier = Modifier, tamano: Dp = 44.dp) {
    val color = colorPuntaje(puntaje)
    Box(
        modifier.size(tamano).clip(CircleShape).background(color.copy(alpha = 0.16f)).border(2.dp, color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(puntaje?.toString() ?: "–", color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
    }
}

// Elemento de la barra de navegación inferior.
data class ItemNavegacion(val etiqueta: String, val icono: ImageVector, val insignia: Int = 0)

// Barra de navegación inferior flotante, en forma de cápsula de vidrio.
@Composable
fun BarraNavegacionVidrio(items: List<ItemNavegacion>, seleccionado: Int, onSeleccionar: (Int) -> Unit) {
    Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 14.dp, vertical = 10.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .shadow(18.dp, FormaCapsula, ambientColor = AzulAndino.copy(alpha = 0.18f), spotColor = AzulAndino.copy(alpha = 0.25f))
                .clip(FormaCapsula)
                .background(Brush.verticalGradient(listOf(VidrioAlto, BlancoVidrio.copy(alpha = 0.70f))))
                .border(1.dp, VidrioBorde, FormaCapsula)
                .padding(6.dp)
        ) {
            items.forEachIndexed { i, item ->
                val activo = i == seleccionado
                Column(
                    Modifier
                        .weight(1f)
                        .clip(FormaCapsula)
                        .background(if (activo) TurquesaLago.copy(alpha = 0.16f) else Color.Transparent)
                        .clickable { onSeleccionar(i) }
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val tinte = if (activo) AzulAndino else TextoSecundario
                    if (item.insignia > 0) {
                        BadgedBox(badge = { Badge(containerColor = RojoError) { Text("${item.insignia}") } }) {
                            Icon(item.icono, contentDescription = item.etiqueta, tint = tinte)
                        }
                    } else {
                        Icon(item.icono, contentDescription = item.etiqueta, tint = tinte)
                    }
                    Text(
                        item.etiqueta,
                        style = MaterialTheme.typography.labelMedium,
                        color = tinte,
                        fontWeight = if (activo) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// Fila "etiqueta: valor" para fichas y detalles.
@Composable
fun FilaDato(etiqueta: String, valor: String, modifier: Modifier = Modifier, destacado: Boolean = false) {
    Row(modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(etiqueta, color = TextoSecundario, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(
            valor,
            color = TextoPrincipal,
            style = if (destacado) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (destacado) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.3f)
        )
    }
}

// Ícono dentro de un círculo de color suave.
@Composable
fun IconoCirculo(icono: ImageVector, color: Color, tamano: Dp = 40.dp) {
    Box(
        Modifier.size(tamano).clip(CircleShape).background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) { Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(tamano * 0.55f)) }
}

// Mensaje breve (éxito o error) en una cápsula.
@Composable
fun MensajeVidrio(texto: String, esError: Boolean = false, onCerrar: (() -> Unit)? = null) {
    val color = if (esError) RojoError else TurquesaLago
    TarjetaVidrio(tinte = color, relleno = 12.dp, onClick = onCerrar) {
        Text(texto, color = if (esError) RojoError else AzulAndino, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

// Botón de ícono simple con el color de marca (para filas de acciones).
@Composable
fun AccionIcono(icono: ImageVector, descripcion: String, onClick: () -> Unit, color: Color = AzulAndino) {
    IconButton(onClick = onClick) { Icon(icono, contentDescription = descripcion, tint = color) }
}
