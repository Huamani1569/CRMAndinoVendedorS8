package com.crmandino.vendedor.model

// Estado de SINCRONIZACIÓN de un registro u operación (separado de la etapa del negocio).
//   PENDIENTE    → guardado en el celular, aún no enviado al servidor
//   ENVIANDO     → se está enviando a la API en este momento
//   SINCRONIZADO → el servidor lo aceptó (tiene identificador remoto)
//   ERROR        → el servidor lo rechazó (permiso, conflicto o validación); el usuario decide qué hacer
enum class EstadoSincronizacion(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    ENVIANDO("Enviando"),
    SINCRONIZADO("Sincronizado"),
    ERROR("Error");

    companion object {
        fun desdeTexto(texto: String): EstadoSincronizacion =
            entries.find { it.name == texto } ?: PENDIENTE
    }
}

// Tipo de operación guardada en la cola local.
object TipoOperacion {
    const val CREAR = "CREAR"
    const val ACTUALIZAR = "ACTUALIZAR"
    const val ELIMINAR = "ELIMINAR"
}

// Entidad a la que pertenece cada operación pendiente.
object EntidadSync {
    const val LEAD = "lead"
    const val INTERACCION = "interaccion"
    const val TAREA = "tarea"
}
