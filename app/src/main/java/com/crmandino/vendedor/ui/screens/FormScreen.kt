package com.crmandino.vendedor.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.crmandino.vendedor.model.Lead
import com.crmandino.vendedor.model.UsuarioSesion
import com.crmandino.vendedor.ui.components.BarraVidrio
import com.crmandino.vendedor.ui.components.BotonCapsula
import com.crmandino.vendedor.ui.components.BotonCapsulaSecundario
import com.crmandino.vendedor.ui.components.CampoVidrio
import com.crmandino.vendedor.ui.components.Capsula
import com.crmandino.vendedor.ui.components.FondoAndino
import com.crmandino.vendedor.ui.components.TarjetaVidrio
import com.crmandino.vendedor.ui.theme.TextoSecundario
import com.crmandino.vendedor.ui.theme.TurquesaLago
import com.crmandino.vendedor.util.Ubicacion
import com.crmandino.vendedor.viewmodel.LeadViewModel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.util.Locale

// Crear (leadId = 0) o editar (leadId > 0) un lead. La empresa es obligatoria; el RUC se necesita para GANADO.
// "Usar mi ubicación" guarda las coordenadas del cliente para la ruta y el check-in.
@Composable
fun FormScreen(
    leadId: Int,
    usuario: UsuarioSesion,
    leadViewModel: LeadViewModel,
    onGuardado: () -> Unit,
    onAtras: () -> Unit
) {
    val context = LocalContext.current
    val alcance = rememberCoroutineScope()
    val esNuevo = leadId == 0
    val leadEditar by remember(leadId) {
        if (esNuevo) flowOf<Lead?>(null) else leadViewModel.leadPorId(leadId)
    }.collectAsState(initial = null)
    val k = leadEditar?.id

    var empresa by rememberSaveable(k) { mutableStateOf(leadEditar?.empresa ?: "") }
    var contacto by rememberSaveable(k) { mutableStateOf(leadEditar?.contacto ?: "") }
    var telefono by rememberSaveable(k) { mutableStateOf(leadEditar?.telefono ?: "") }
    var correo by rememberSaveable(k) { mutableStateOf(leadEditar?.correo ?: "") }
    var ruc by rememberSaveable(k) { mutableStateOf(leadEditar?.ruc ?: "") }
    var direccion by rememberSaveable(k) { mutableStateOf(leadEditar?.direccion ?: "") }
    var valor by rememberSaveable(k) {
        mutableStateOf(leadEditar?.valorPotencial?.let { if (it % 1.0 == 0.0) "${it.toLong()}" else "$it" } ?: "")
    }
    var latitud by remember(k) { mutableStateOf(leadEditar?.latitud) }
    var longitud by remember(k) { mutableStateOf(leadEditar?.longitud) }
    var leyendoGps by remember { mutableStateOf(false) }
    var avisoGps by remember { mutableStateOf<String?>(null) }
    val e = leadViewModel.erroresFormulario

    fun leerUbicacion() {
        leyendoGps = true
        alcance.launch {
            val u = Ubicacion.obtener(context)
            leyendoGps = false
            if (u == null) avisoGps = "No se pudo leer el GPS (actívalo e inténtalo de nuevo)."
            else { latitud = u.latitude; longitud = u.longitude; avisoGps = null }
        }
    }
    val permiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { r ->
        if (r.values.any { it }) leerUbicacion() else avisoGps = "Sin permiso de ubicación."
    }

    LaunchedEffect(Unit) { leadViewModel.limpiarErrores() }

    FondoAndino {
        Column(Modifier.fillMaxSize().imePadding()) {
            BarraVidrio(if (esNuevo) "Nuevo lead" else "Editar lead", "Vendedor: ${usuario.nombre}", onAtras = onAtras)
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TarjetaVidrio {
                    Text("Empresa", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    CampoVidrio(empresa, { empresa = it }, "Empresa *", error = e.empresa, icono = Icons.Filled.Business)
                    Spacer(Modifier.height(8.dp))
                    CampoVidrio(ruc, { ruc = it.filter { c -> c.isDigit() }.take(11) }, "RUC (necesario para cerrar la venta)", error = e.ruc,
                        icono = Icons.Filled.Badge, teclado = KeyboardOptions(keyboardType = KeyboardType.Number))
                    Spacer(Modifier.height(8.dp))
                    CampoVidrio(valor, { valor = it }, "Valor potencial (S/)", error = e.valor, icono = Icons.Filled.AttachMoney,
                        teclado = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                }
                TarjetaVidrio {
                    Text("Contacto", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    CampoVidrio(contacto, { contacto = it }, "Nombre del contacto", icono = Icons.Filled.Person)
                    Spacer(Modifier.height(8.dp))
                    CampoVidrio(telefono, { telefono = it.filter { c -> c.isDigit() }.take(9) }, "Celular", error = e.telefono,
                        icono = Icons.Filled.PhoneAndroid, teclado = KeyboardOptions(keyboardType = KeyboardType.Phone))
                    Spacer(Modifier.height(8.dp))
                    CampoVidrio(correo, { correo = it }, "Correo", error = e.correo, icono = Icons.Filled.Email,
                        teclado = KeyboardOptions(keyboardType = KeyboardType.Email))
                }
                TarjetaVidrio {
                    Text("Ubicación", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    CampoVidrio(direccion, { direccion = it }, "Dirección", icono = Icons.Filled.Place)
                    Spacer(Modifier.height(8.dp))
                    val lat = latitud
                    val lon = longitud
                    if (lat != null && lon != null) {
                        Capsula(String.format(Locale.US, "GPS: %.5f, %.5f", lat, lon), TurquesaLago, icono = Icons.Filled.Place)
                        Spacer(Modifier.height(8.dp))
                    }
                    BotonCapsulaSecundario(
                        if (leyendoGps) "Leyendo GPS…" else "Usar mi ubicación actual", {
                            if (Ubicacion.tienePermiso(context)) leerUbicacion()
                            else permiso.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                        },
                        Modifier.fillMaxWidth(), icono = Icons.Filled.MyLocation, color = TurquesaLago, habilitado = !leyendoGps
                    )
                    Text("Úsalo cuando estés en el local del cliente: así aparece en el mapa de tu ruta.",
                        style = MaterialTheme.typography.labelMedium, color = TextoSecundario, modifier = Modifier.padding(top = 4.dp))
                    avisoGps?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium) }
                }
                if (esNuevo) {
                    Text("El lead empieza en la etapa NUEVO; el servidor calcula su puntaje al sincronizar.",
                        style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
                }
            }
            BotonCapsula(
                "Guardar", {
                    leadViewModel.guardarLead(leadId, empresa, contacto, telefono, correo, ruc, direccion, valor, latitud, longitud, onGuardado)
                },
                Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp), icono = Icons.Filled.Save
            )
        }
    }
}
