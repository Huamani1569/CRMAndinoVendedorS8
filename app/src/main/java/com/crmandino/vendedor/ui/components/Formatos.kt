package com.crmandino.vendedor.ui.components

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Da formato de moneda peruana: 95000.0 -> "S/ 95,000"
fun formatearSoles(valor: Double): String =
    "S/ " + String.format(Locale.US, "%,.0f", valor)

// Fecha de hoy + N días en formato dd/mm/aaaa (valor sugerido en los formularios).
fun fechaDentroDe(dias: Int): String {
    val calendario = Calendar.getInstance()
    calendario.add(Calendar.DAY_OF_YEAR, dias)
    return SimpleDateFormat("dd/MM/yyyy", Locale.US).format(calendario.time)
}

// 1791334737131 -> "06/10/2026 20:15:03"
fun formatearFechaHora(milis: Long?): String =
    if (milis == null) "nunca" else SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US).format(Date(milis))
