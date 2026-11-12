package com.crmandino.vendedor.model

// Etapas del embudo del Proyecto Final (mismas que la API y la base de datos).
// En Room y en la API se guarda el CÓDIGO (NUEVO, CONTACTADO...); en pantalla se muestra la etiqueta.
enum class EtapaVenta(val etiqueta: String) {
    NUEVO("Nuevo"),
    CONTACTADO("Contactado"),
    CALIFICADO("Calificado"),
    COTIZADO("Cotizado"),
    GANADO("Ganado"),
    PERDIDO("Perdido");

    val abierta: Boolean get() = this != GANADO && this != PERDIDO

    // Etapas a las que el vendedor puede pasar a mano (mismas reglas que el trigger reglas_lead).
    // COTIZADO no se elige a mano: se alcanza al enviar una cotización.
    fun siguientes(): List<EtapaVenta> = when (this) {
        NUEVO -> listOf(CONTACTADO, CALIFICADO, GANADO, PERDIDO)
        CONTACTADO -> listOf(CALIFICADO, GANADO, PERDIDO)
        CALIFICADO -> listOf(GANADO, PERDIDO)
        COTIZADO -> listOf(CALIFICADO, GANADO, PERDIDO)
        PERDIDO -> listOf(NUEVO)
        GANADO -> emptyList()
    }

    companion object {
        // Acepta el código o la etiqueta; también las etapas de la Semana 8.
        fun desdeTexto(texto: String?): EtapaVenta = when (texto) {
            null -> NUEVO
            "Prospecto" -> NUEVO
            "Propuesta" -> CALIFICADO
            "Negociación" -> COTIZADO
            "Cerrado" -> GANADO
            else -> entries.find { it.name == texto || it.etiqueta == texto } ?: NUEVO
        }

        val codigos: List<String> = entries.map { it.name }
        val abiertas: List<EtapaVenta> = entries.filter { it.abierta }
    }
}
