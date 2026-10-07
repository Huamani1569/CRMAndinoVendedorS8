package com.crmandino.vendedor.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.crmandino.vendedor.model.EtapaVenta
import com.crmandino.vendedor.model.Lead
import com.crmandino.vendedor.model.UsuarioSesion
import com.crmandino.vendedor.ui.components.BarraSuperior
import com.crmandino.vendedor.ui.components.InputField
import com.crmandino.vendedor.ui.components.SelectorChips
import com.crmandino.vendedor.viewmodel.LeadViewModel
import kotlinx.coroutines.flow.flowOf

// Crear (leadId = 0) o editar (leadId > 0) un lead. RF10: la empresa es obligatoria.
@Composable
fun FormScreen(
    leadId: Int,
    usuario: UsuarioSesion,
    leadViewModel: LeadViewModel,
    onGuardado: () -> Unit,
    onAtras: () -> Unit
) {
    val esNuevo = leadId == 0
    val leadEditar by remember(leadId) {
        if (esNuevo) flowOf<Lead?>(null) else leadViewModel.leadPorId(leadId)
    }.collectAsState(initial = null)

    // Los campos se llenan cuando el lead a editar llega desde Room.
    var empresa by rememberSaveable(leadEditar?.id) { mutableStateOf(leadEditar?.empresa ?: "") }
    var contacto by rememberSaveable(leadEditar?.id) { mutableStateOf(leadEditar?.contacto ?: "") }
    var valor by rememberSaveable(leadEditar?.id) {
        mutableStateOf(leadEditar?.valorPotencial?.let { if (it % 1.0 == 0.0) "${it.toLong()}" else "$it" } ?: "")
    }
    var etapa by rememberSaveable(leadEditar?.id) {
        mutableStateOf(leadEditar?.etapa ?: EtapaVenta.PROSPECTO.etiqueta)
    }

    // Al abrir el formulario no se muestran errores anteriores.
    LaunchedEffect(Unit) { leadViewModel.limpiarErrores() }

    Scaffold(
        topBar = { BarraSuperior(titulo = if (esNuevo) "Nuevo lead" else "Editar lead", onAtras = onAtras) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(24.dp)
        ) {
            InputField(
                valor = empresa,
                onValorChange = { empresa = it; leadViewModel.limpiarErrores() },
                etiqueta = "Empresa *",
                iconoInicio = { Icon(Icons.Filled.Business, contentDescription = null) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            InputField(
                valor = contacto,
                onValorChange = { contacto = it },
                etiqueta = "Persona de contacto",
                iconoInicio = { Icon(Icons.Filled.Person, contentDescription = null) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            InputField(
                valor = valor,
                onValorChange = { valor = it; leadViewModel.limpiarErrores() },
                etiqueta = "Valor potencial (S/)",
                iconoInicio = { Icon(Icons.Filled.MonetizationOn, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done)
            )

            Text("Etapa del embudo", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(6.dp))
            SelectorChips(opciones = EtapaVenta.etiquetas, seleccionada = etapa, onSeleccionar = { etapa = it })

            Spacer(Modifier.height(12.dp))
            Text(
                "Vendedor: ${leadEditar?.vendedorNombre ?: usuario.nombre}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Mensaje de error del ViewModel (RF10)
            leadViewModel.errorFormulario?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(Modifier.height(20.dp))
            BotonPrincipal(
                texto = "Guardar lead",
                cargando = false,
                onClick = {
                    leadViewModel.guardarLead(
                        id = leadId,
                        empresa = empresa,
                        contacto = contacto,
                        valorTexto = valor,
                        etapa = etapa,
                        onGuardado = onGuardado
                    )
                }
            )
        }
    }
}
