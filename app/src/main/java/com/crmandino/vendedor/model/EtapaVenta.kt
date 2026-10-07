package com.crmandino.vendedor.model

// Etapas del embudo de ventas, en el orden en que avanza un lead.
// En Room la etapa se guarda como texto (la etiqueta: "Prospecto", "Negociación", ...).
enum class EtapaVenta(val etiqueta: String) {
    PROSPECTO("Prospecto"),
    CONTACTADO("Contactado"),
    PROPUESTA("Propuesta"),
    NEGOCIACION("Negociación"),
    CERRADO("Cerrado");

    companion object {
        // Convierte el texto guardado en la base de datos de vuelta a la etapa.
        fun desdeTexto(texto: String): EtapaVenta =
            entries.find { it.etiqueta == texto } ?: PROSPECTO

        val etiquetas: List<String> = entries.map { it.etiqueta }
    }
}
