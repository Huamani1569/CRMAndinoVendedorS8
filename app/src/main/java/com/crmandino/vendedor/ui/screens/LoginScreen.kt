package com.crmandino.vendedor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.crmandino.vendedor.model.RolUsuario
import com.crmandino.vendedor.ui.components.AppLogo
import com.crmandino.vendedor.ui.components.InputField
import com.crmandino.vendedor.ui.theme.AzulAndino
import com.crmandino.vendedor.ui.theme.AzulAndinoOscuro
import com.crmandino.vendedor.ui.theme.NieveAndina
import com.crmandino.vendedor.viewmodel.AuthViewModel

// Login: valida contra la API REST (POST /api/auth/login). La lógica está en AuthViewModel.
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginExitoso: () -> Unit
) {
    var correo by rememberSaveable { mutableStateOf("") }
    var clave by rememberSaveable { mutableStateOf("") }
    var claveVisible by remember { mutableStateOf(false) }
    var mostrarServidor by rememberSaveable { mutableStateOf(false) }
    var servidor by rememberSaveable { mutableStateOf(viewModel.servidor) }

    // Cuando el ViewModel encuentra al usuario en Room, se pasa al inicio.
    LaunchedEffect(viewModel.usuarioActual) {
        if (viewModel.usuarioActual != null) onLoginExitoso()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .imePadding()
    ) {
        CabeceraMarca()

        // ---------- Formulario ----------
        Column(Modifier.padding(24.dp)) {
            Text("Iniciar sesión", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Ingresa con tu cuenta de ${RolUsuario.ETIQUETA.lowercase()}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))

            InputField(
                valor = correo,
                onValorChange = { correo = it; viewModel.limpiarErrores() },
                etiqueta = "Correo electrónico",
                error = viewModel.errorCorreo,
                habilitado = !viewModel.cargando,
                iconoInicio = { Icon(Icons.Filled.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )
            Spacer(Modifier.height(4.dp))
            InputField(
                valor = clave,
                onValorChange = { clave = it; viewModel.limpiarErrores() },
                etiqueta = "Contraseña",
                error = viewModel.errorClave,
                habilitado = !viewModel.cargando,
                iconoInicio = { Icon(Icons.Filled.Lock, contentDescription = null) },
                iconoFin = {
                    IconButton(onClick = { claveVisible = !claveVisible }) {
                        Icon(
                            if (claveVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (claveVisible) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                },
                visualTransformation = if (claveVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                )
            )

            // Mensaje de error general (usuario inexistente o clave incorrecta)
            viewModel.mensajeError?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(Modifier.height(12.dp))
            BotonPrincipal(
                texto = "Ingresar",
                cargando = viewModel.cargando,
                textoCargando = viewModel.textoCarga,
                onClick = { viewModel.iniciarSesion(correo, clave) }
            )

            Spacer(Modifier.height(8.dp))
            Text(
                "Las cuentas las crea el administrador en el servidor. Sin conexión solo puede ingresar " +
                    "quien ya inició sesión antes en este celular.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Dirección de la API REST (se puede cambiar sin recompilar la app)
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Dns, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = { mostrarServidor = !mostrarServidor }, enabled = !viewModel.cargando) {
                    Text("Servidor: ${viewModel.servidor}")
                }
            }
            if (mostrarServidor) {
                OutlinedTextField(
                    value = servidor,
                    onValueChange = { servidor = it },
                    label = { Text("URL de la API (emulador: http://10.0.2.2:3000/)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = {
                        viewModel.restablecerServidor()
                        servidor = viewModel.servidor
                    }) { Text("Por defecto") }
                    TextButton(onClick = {
                        viewModel.guardarServidor(servidor)
                        servidor = viewModel.servidor
                        mostrarServidor = false
                    }) { Text("Guardar", fontWeight = FontWeight.Bold) }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// Cabecera con la identidad visual (logo + nombre), compartida por Login y Registro.
@Composable
fun CabeceraMarca() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(AzulAndino, AzulAndinoOscuro)),
                RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            )
            .statusBarsPadding()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppLogo(size = 84.dp)
        Spacer(Modifier.height(12.dp))
        Text("CRM Andino Perú", style = MaterialTheme.typography.titleLarge, color = Color.White)
        Text("App del ${RolUsuario.ETIQUETA.lowercase()}", color = NieveAndina)
    }
}

// Botón grande con indicador de carga.
@Composable
fun BotonPrincipal(texto: String, cargando: Boolean, onClick: () -> Unit, textoCargando: String = "Guardando...") {
    Button(
        onClick = onClick,
        enabled = !cargando,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        if (cargando) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.size(10.dp))
            Text(textoCargando)
        } else {
            Text(texto, fontWeight = FontWeight.Bold)
        }
    }
}
