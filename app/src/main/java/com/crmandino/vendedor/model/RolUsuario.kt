package com.crmandino.vendedor.model

// Rol que puede entrar en ESTA app.
// Según la guía del curso, el rol "cliente" representa al VENDEDOR en el caso CRM Andino Perú.
object RolUsuario {
    const val VALOR = "cliente"      // Valor de la columna "rol" en la tabla usuarios (servidor y Room)
    const val ETIQUETA = "Vendedor"  // Lo que se muestra en pantalla
}
