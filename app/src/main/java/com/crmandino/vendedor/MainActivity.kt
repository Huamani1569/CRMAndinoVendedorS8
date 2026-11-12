package com.crmandino.vendedor

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import com.crmandino.vendedor.util.BarrasDelSistema
import com.crmandino.vendedor.util.prepararTema
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.crmandino.vendedor.di.AppContainer
import com.crmandino.vendedor.ui.navigation.AppNavigation
import com.crmandino.vendedor.ui.theme.CRMAndinoTheme
import com.crmandino.vendedor.viewmodel.AuthViewModel
import com.crmandino.vendedor.viewmodel.InteraccionViewModel
import com.crmandino.vendedor.viewmodel.LeadViewModel
import com.crmandino.vendedor.viewmodel.SyncViewModel
import com.crmandino.vendedor.viewmodel.TareaViewModel
import com.crmandino.vendedor.viewmodel.VentasViewModel

// Punto de entrada: crea los ViewModel con los repositorios del contenedor (di/AppContainer).
class MainActivity : ComponentActivity() {

    private val contenedor: AppContainer
        get() = (application as CrmVendedorApplication).contenedor

    private val authViewModel: AuthViewModel by viewModels {
        viewModelFactory { initializer { AuthViewModel(contenedor.authRepository, contenedor.syncRepository) } }
    }

    private val leadViewModel: LeadViewModel by viewModels {
        viewModelFactory { initializer { LeadViewModel(contenedor.leadRepository) } }
    }

    private val interaccionViewModel: InteraccionViewModel by viewModels {
        viewModelFactory { initializer { InteraccionViewModel(contenedor.interaccionRepository) } }
    }

    private val tareaViewModel: TareaViewModel by viewModels {
        viewModelFactory { initializer { TareaViewModel(contenedor.tareaRepository) } }
    }

    private val syncViewModel: SyncViewModel by viewModels {
        viewModelFactory {
            initializer {
                SyncViewModel(contenedor.syncRepository, contenedor.conectado, contenedor::sincronizarEnSegundoPlano)
            }
        }
    }

    private val ventasViewModel: VentasViewModel by viewModels {
        viewModelFactory { initializer { VentasViewModel(contenedor.ventasRepository, contenedor.conectado) } }
    }

    // Se incrementa cada vez que el usuario toca una notificación del sistema
    private val abrirNotificaciones = mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prepararTema()   // modo claro / oscuro guardado
        if (intent?.getBooleanExtra("abrir_notificaciones", false) == true) abrirNotificaciones.intValue = 1
        setContent {
            CRMAndinoTheme {
                BarrasDelSistema()
                AppNavigation(
                    authViewModel, leadViewModel, interaccionViewModel, tareaViewModel, syncViewModel,
                    ventasViewModel, abrirNotificaciones.intValue
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.getBooleanExtra("abrir_notificaciones", false)) abrirNotificaciones.intValue += 1
    }
}
