package com.crmandino.vendedor.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.crmandino.vendedor.ui.components.EstadoConexionUi
import com.crmandino.vendedor.ui.components.LocalEstadoConexion
import com.crmandino.vendedor.ui.screens.DetailScreen
import com.crmandino.vendedor.ui.screens.FormScreen
import com.crmandino.vendedor.ui.screens.HomeScreen
import com.crmandino.vendedor.ui.screens.ListScreen
import com.crmandino.vendedor.ui.screens.LoginScreen
import com.crmandino.vendedor.ui.screens.SplashScreen
import com.crmandino.vendedor.ui.screens.SyncScreen
import com.crmandino.vendedor.viewmodel.AuthViewModel
import com.crmandino.vendedor.viewmodel.InteraccionViewModel
import com.crmandino.vendedor.viewmodel.LeadViewModel
import com.crmandino.vendedor.viewmodel.SyncViewModel
import com.crmandino.vendedor.viewmodel.TareaViewModel

// Pantallas de la app Vendedor.
enum class Pantalla { SPLASH, LOGIN, HOME, LISTA, DETALLE, FORMULARIO, SINCRONIZACION }

/**
 * Flujo: Splash → Login (o directo al Home si hay sesión guardada) → Home → Lista → Detalle → Formulario,
 * y la pantalla de Sincronización desde el Home.
 * Sin librería de navegación: la pantalla actual se guarda en una variable de estado.
 */
@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    leadViewModel: LeadViewModel,
    interaccionViewModel: InteraccionViewModel,
    tareaViewModel: TareaViewModel,
    syncViewModel: SyncViewModel
) {
    var pantalla by rememberSaveable { mutableStateOf(Pantalla.SPLASH) }
    var leadId by rememberSaveable { mutableIntStateOf(0) }            // Lead abierto (0 = nuevo)
    var origenDetalle by rememberSaveable { mutableStateOf(Pantalla.LISTA) }
    var origenFormulario by rememberSaveable { mutableStateOf(Pantalla.LISTA) }
    var etapaFiltro by rememberSaveable { mutableStateOf<String?>(null) }
    val usuario = authViewModel.usuarioActual

    // Estado de conexión para la franja "Sin conexión" de todas las pantallas
    val conectado by syncViewModel.conectado.collectAsState()
    val pendientes by syncViewModel.pendientes.collectAsState(initial = 0)
    val errores by syncViewModel.errores.collectAsState(initial = 0)

    val abrirDetalle = { id: Int, desde: Pantalla ->
        leadId = id
        origenDetalle = desde
        pantalla = Pantalla.DETALLE
    }
    val abrirFormulario = { id: Int, desde: Pantalla ->
        leadId = id
        origenFormulario = desde
        pantalla = Pantalla.FORMULARIO
    }

    // Botón "atrás" del sistema
    BackHandler(enabled = pantalla !in listOf(Pantalla.SPLASH, Pantalla.LOGIN, Pantalla.HOME)) {
        pantalla = when (pantalla) {
            Pantalla.DETALLE -> origenDetalle
            Pantalla.FORMULARIO -> origenFormulario
            else -> Pantalla.HOME
        }
    }

    // Sin sesión activa no se puede entrar a las pantallas internas.
    if (usuario == null && pantalla !in listOf(Pantalla.SPLASH, Pantalla.LOGIN)) {
        LaunchedEffect(Unit) { pantalla = Pantalla.LOGIN }
        return
    }

    CompositionLocalProvider(LocalEstadoConexion provides EstadoConexionUi(conectado, pendientes, errores)) {
        when (pantalla) {
            // Si quedó una sesión guardada (incluso sin conexión) se entra directo al inicio.
            Pantalla.SPLASH -> SplashScreen(onFinalizar = {
                pantalla = if (authViewModel.usuarioActual != null) Pantalla.HOME else Pantalla.LOGIN
            })

            Pantalla.LOGIN -> LoginScreen(
                viewModel = authViewModel,
                onLoginExitoso = { pantalla = Pantalla.HOME }
            )

            Pantalla.HOME -> HomeScreen(
                usuario = usuario!!,
                leadViewModel = leadViewModel,
                tareaViewModel = tareaViewModel,
                syncViewModel = syncViewModel,
                avisoSinConexion = authViewModel.avisoSinConexion,
                onDescartarAviso = authViewModel::descartarAviso,
                onVerLeads = { etapa ->
                    etapaFiltro = etapa
                    pantalla = Pantalla.LISTA
                },
                onNuevoLead = { abrirFormulario(0, Pantalla.HOME) },
                onAbrirLead = { abrirDetalle(it, Pantalla.HOME) },
                onVerSincronizacion = { pantalla = Pantalla.SINCRONIZACION },
                onCerrarSesion = {
                    authViewModel.cerrarSesion()
                    pantalla = Pantalla.LOGIN
                }
            )

            Pantalla.LISTA -> ListScreen(
                leadViewModel = leadViewModel,
                etapaFiltro = etapaFiltro,
                onCambiarFiltro = { etapaFiltro = it },
                onAbrirLead = { abrirDetalle(it, Pantalla.LISTA) },
                onNuevoLead = { abrirFormulario(0, Pantalla.LISTA) },
                onAtras = { pantalla = Pantalla.HOME }
            )

            Pantalla.DETALLE -> DetailScreen(
                leadId = leadId,
                leadViewModel = leadViewModel,
                interaccionViewModel = interaccionViewModel,
                tareaViewModel = tareaViewModel,
                onEditar = { abrirFormulario(leadId, Pantalla.DETALLE) },
                onAtras = { pantalla = origenDetalle }
            )

            Pantalla.FORMULARIO -> FormScreen(
                leadId = leadId,
                usuario = usuario!!,
                leadViewModel = leadViewModel,
                onGuardado = { pantalla = origenFormulario },
                onAtras = { pantalla = origenFormulario }
            )

            Pantalla.SINCRONIZACION -> SyncScreen(
                syncViewModel = syncViewModel,
                onAtras = { pantalla = Pantalla.HOME }
            )
        }
    }
}
