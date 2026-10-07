package com.crmandino.vendedor

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        setContent {
            CRMAndinoTheme {
                AppNavigation(authViewModel, leadViewModel, interaccionViewModel, tareaViewModel, syncViewModel)
            }
        }
    }
}
