package com.crmandino.vendedor.model

// Rol que puede entrar en ESTA app. En el Proyecto Final la API usa roles con su nombre real.
object RolUsuario {
    const val VALOR = "vendedor"      // columna "rol" de la tabla usuarios
    const val APP = "VENDEDOR"        // la API valida con qué app se ingresa
    const val ETIQUETA = "Vendedor de campo"
}
