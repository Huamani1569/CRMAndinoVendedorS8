package com.crmandino.vendedor.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// Formatos de moneda y fechas que usan las pantallas del Proyecto Final.
object Formato {
    private val lima: TimeZone = TimeZone.getTimeZone("America/Lima")

    // 12500.5 → "S/ 12,500.50"
    fun soles(valor: Double?, decimales: Boolean = true): String =
        if (valor == null) "—"
        else "S/ " + String.format(Locale.US, if (decimales) "%,.2f" else "%,.0f", valor)

    // 450000 → "S/ 450 mil" (para indicadores)
    fun solesCorto(valor: Double?): String = when {
        valor == null -> "—"
        valor >= 1_000_000 -> "S/ " + String.format(Locale.US, "%.1f", valor / 1_000_000) + " M"
        valor >= 10_000 -> "S/ " + String.format(Locale.US, "%.0f", valor / 1000) + " mil"
        else -> soles(valor, false)
    }

    fun porcentaje(valor: Double?): String = if (valor == null) "—" else String.format(Locale.US, "%.1f %%", valor)

    // Acepta "2026-11-11", "2026-11-11T15:04:05.123Z" o con zona horaria
    fun aFecha(iso: String?): Date? {
        if (iso.isNullOrBlank()) return null
        val patrones = listOf("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", "yyyy-MM-dd'T'HH:mm:ssXXX", "yyyy-MM-dd")
        for (p in patrones) {
            val f = SimpleDateFormat(p, Locale.US)
            if (p == "yyyy-MM-dd") f.timeZone = lima
            val d = runCatching { f.parse(iso) }.getOrNull()
            if (d != null) return d
        }
        return null
    }

    // "2026-11-11" → "11/11/2026"
    fun fecha(iso: String?): String {
        val d = aFecha(iso) ?: return iso ?: "—"
        return SimpleDateFormat("dd/MM/yyyy", Locale.US).apply { timeZone = lima }.format(d)
    }

    // "2026-11-11T20:04:05Z" → "11/11/2026 15:04"
    fun fechaHora(iso: String?): String {
        val d = aFecha(iso) ?: return iso ?: "—"
        return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).apply { timeZone = lima }.format(d)
    }

    // "hace 5 min", "hace 2 h", "hace 3 días"
    fun haceCuanto(iso: String?): String {
        val d = aFecha(iso) ?: return ""
        val seg = (System.currentTimeMillis() - d.time) / 1000
        return when {
            seg < 60 -> "hace un momento"
            seg < 3600 -> "hace ${seg / 60} min"
            seg < 86400 -> "hace ${seg / 3600} h"
            seg < 86400 * 30 -> "hace ${seg / 86400} días"
            else -> fecha(iso)
        }
    }

    // Horas que faltan (negativo = vencido) hasta una fecha ISO
    fun horasHasta(iso: String?): Double? {
        val d = aFecha(iso) ?: return null
        return (d.time - System.currentTimeMillis()) / 3_600_000.0
    }

    // Fecha de hoy en Lima, en formato ISO (aaaa-mm-dd)
    fun hoyIso(diasMas: Int = 0): String {
        val c = Calendar.getInstance(lima)
        c.add(Calendar.DAY_OF_YEAR, diasMas)
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = lima }.format(c.time)
    }

    // Fecha y hora actuales en ISO-8601 UTC (para check-in e interacciones)
    fun ahoraIso(): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date())
}
