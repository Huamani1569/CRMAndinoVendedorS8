package com.crmandino.vendedor.data.mapper

// La app muestra las fechas como dd/mm/aaaa; la API las recibe y envía como aaaa-mm-dd (ISO).
object FechaMapper {

    // "2026-10-08" → "08/10/2026"
    fun isoAVisible(iso: String): String {
        val partes = iso.take(10).split("-")
        return if (partes.size == 3) "${partes[2]}/${partes[1]}/${partes[0]}" else iso
    }

    // "08/10/2026" → "2026-10-08"
    fun visibleAIso(fecha: String): String {
        val partes = fecha.trim().split("/")
        return if (partes.size == 3) "${partes[2]}-${partes[1]}-${partes[0]}" else fecha
    }
}
