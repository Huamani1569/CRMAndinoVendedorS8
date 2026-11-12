package com.crmandino.vendedor.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crmandino.vendedor.data.repository.VentasRepository
import com.crmandino.vendedor.model.Lead
import com.crmandino.vendedor.ui.components.CampoVidrio
import com.crmandino.vendedor.ui.components.Capsula
import com.crmandino.vendedor.ui.components.FilaDato
import com.crmandino.vendedor.ui.theme.RojoError
import com.crmandino.vendedor.ui.theme.TextoSecundario
import com.crmandino.vendedor.ui.theme.TurquesaLago
import com.crmandino.vendedor.ui.theme.VerdeExito
import com.crmandino.vendedor.util.Ubicacion
import com.crmandino.vendedor.viewmodel.PreCheckin
import com.crmandino.vendedor.viewmodel.VentasViewModel
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Flujo de CHECK-IN (RF-V12, RF-V13): pide el permiso de ubicación, lee el GPS, calcula la distancia
 * al cliente y, si está a más de 300 m, exige el motivo. Funciona sin conexión (queda en la cola).
 * Devuelve la función que inicia el check-in para un lead.
 */
@Composable
fun rememberCheckin(vm: VentasViewModel): (Lead) -> Unit {
    val context = LocalContext.current
    val alcance = rememberCoroutineScope()
    var leadPendiente by remember { mutableStateOf<Lead?>(null) }
    var pre by remember { mutableStateOf<PreCheckin?>(null) }
    var buscando by remember { mutableStateOf(false) }

    fun leerGps(lead: Lead) {
        if (!Ubicacion.gpsActivo(context)) {
            vm.avisar("Activa la ubicación (GPS) del celular para hacer el check-in.", true)
            return
        }
        buscando = true
        alcance.launch {
            val ubicacion = Ubicacion.obtener(context)
            buscando = false
            if (ubicacion == null) vm.avisar("No se pudo leer el GPS. Sal a un lugar abierto e inténtalo de nuevo.", true)
            else pre = vm.calcularCheckin(lead, ubicacion)
        }
    }

    val permiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { resultado ->
        val lead = leadPendiente
        if (resultado.values.any { it } && lead != null) leerGps(lead)
        else vm.avisar("Sin permiso de ubicación no se puede registrar el check-in.", true)
    }

    if (buscando) {
        AlertDialog(
            onDismissRequest = {},
            icon = { CircularProgressIndicator(color = TurquesaLago, modifier = Modifier.size(32.dp)) },
            title = { Text("Leyendo tu ubicación…") },
            text = { Text("Usamos el GPS del celular para confirmar que estás donde el cliente.") },
            confirmButton = {}
        )
    }

    pre?.let { p ->
        CheckinDialog(
            p = p,
            onConfirmar = { motivo -> vm.confirmarCheckin(p, motivo) { pre = null } },
            onCancelar = { pre = null }
        )
    }

    return { lead ->
        leadPendiente = lead
        if (Ubicacion.tienePermiso(context)) leerGps(lead)
        else permiso.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }
}

@Composable
private fun CheckinDialog(p: PreCheckin, onConfirmar: (String) -> String?, onCancelar: () -> Unit) {
    var motivo by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onCancelar,
        icon = { androidx.compose.material3.Icon(Icons.Filled.GpsFixed, contentDescription = null, tint = TurquesaLago) },
        title = { Text("Check-in en ${p.lead.empresa}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val d = p.distancia
                when {
                    d == null -> Text("El cliente no tiene ubicación registrada: se guardará tu posición.", color = TextoSecundario)
                    p.fueraDeRango -> Capsula("Estás a ${d.toInt()} m · más de ${VentasRepository.RADIO_MAXIMO_M.toInt()} m", RojoError, icono = Icons.Filled.Warning)
                    else -> Capsula("Estás a ${d.toInt()} m del cliente · dentro del radio", VerdeExito, icono = Icons.Filled.CheckCircle)
                }
                FilaDato("Tu posición", String.format(Locale.US, "%.5f, %.5f", p.ubicacion.latitude, p.ubicacion.longitude))
                if (p.ubicacion.hasAccuracy()) FilaDato("Precisión del GPS", "± ${p.ubicacion.accuracy.toInt()} m")
                if (p.fueraDeRango) {
                    CampoVidrio(motivo, { motivo = it; error = null }, "Motivo (obligatorio)", lineas = 2,
                        ayuda = "Ej.: el cliente atendió en su almacén de otra dirección")
                }
                error?.let { Text(it, color = RojoError, style = MaterialTheme.typography.labelMedium) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.width(2.dp))
                    Text("Se registra también como interacción \"Visita\" del lead.", style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { error = onConfirmar(motivo) }) { Text("Registrar check-in", fontWeight = FontWeight.Bold, color = TurquesaLago) }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}
