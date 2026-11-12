package com.crmandino.vendedor.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * MODO CLARO Y OSCURO.
 * Paleta.oscuro es un estado de Compose: al cambiarlo, todas las pantallas que leen estos colores
 * se vuelven a dibujar con la otra paleta. La preferencia (Sistema / Claro / Oscuro) la elige el usuario
 * con el botón ☀/🌙 y se guarda en el celular.
 */
enum class PreferenciaTema { SISTEMA, CLARO, OSCURO }

object Paleta {
    var oscuro by mutableStateOf(false)
    var preferencia by mutableStateOf(PreferenciaTema.SISTEMA)
    var alCambiarPreferencia: ((PreferenciaTema) -> Unit)? = null

    // Sistema → Claro → Oscuro → Sistema ...
    fun siguientePreferencia() {
        preferencia = PreferenciaTema.entries[(preferencia.ordinal + 1) % PreferenciaTema.entries.size]
        alCambiarPreferencia?.invoke(preferencia)
    }
}

private fun tono(claro: Long, oscuro: Long): Color = Color(if (Paleta.oscuro) oscuro else claro)

// ===== Paleta de marca CRM Andino Perú =====
val AzulAndino: Color get() = tono(0xFF1F3A5F, 0xFF6F9FE0)       // Primario: confianza, seriedad empresarial
val AzulAndinoOscuro: Color get() = tono(0xFF142842, 0xFF0B1422)
val TurquesaLago: Color get() = tono(0xFF1B998B, 0xFF2EC4B3)     // Secundario: crecimiento, acciones positivas
val DoradoInca: Color get() = tono(0xFFF2A93B, 0xFFF5B95A)       // Acento: metas, ventas, resaltados
val FondoClaro: Color get() = tono(0xFFF5F7FA, 0xFF0E1521)
val Superficie: Color get() = tono(0xFFFFFFFF, 0xFF1A2332)
val TextoPrincipal: Color get() = tono(0xFF1C2430, 0xFFE9EEF5)
val TextoSecundario: Color get() = tono(0xFF5B6675, 0xFFA7B2C1)
val RojoError: Color get() = tono(0xFFC0392B, 0xFFF0776A)
val NieveAndina: Color get() = tono(0xFFE8EEF5, 0xFF243247)
val VerdeExito: Color get() = tono(0xFF1E9E5A, 0xFF3CCB7F)
val GrisNeutro: Color get() = tono(0xFF8A94A3, 0xFF8792A2)

// ===== "Liquid Glass": superficies translúcidas sobre un fondo con luces suaves =====
val VidrioAlto: Color get() = tono(0xD9FFFFFF, 0xD91E2939)        // 85 %
val VidrioBajo: Color get() = tono(0x94FFFFFF, 0x99141C29)        // 58 %
val VidrioBorde: Color get() = tono(0xFFFFFFFF, 0x2EFFFFFF)       // borde de luz
val SobreColor: Color get() = tono(0xFFFFFFFF, 0xFF0B1422)     // texto sobre botones y cápsulas de color
val BlancoVidrio: Color get() = tono(0xFFFFFFFF, 0xFF2A3749)      // "blanco" de las superficies
val FondoCieloArriba: Color get() = tono(0xFFE6EFF7, 0xFF0D1626)
val FondoCieloMedio: Color get() = tono(0xFFDDEDEE, 0xFF0F1F27)
val FondoCieloAbajo: Color get() = tono(0xFFF6EFE3, 0xFF17161F)

// ===== Colores por etapa del embudo (Proyecto Final) =====
val EtapaNuevo: Color get() = tono(0xFF7F8C9D, 0xFF95A2B3)
val EtapaContactado: Color get() = tono(0xFF3A7BD5, 0xFF6EA2EC)
val EtapaCalificado: Color get() = tono(0xFF8E5CC4, 0xFFB28BE0)
val EtapaCotizado: Color get() = tono(0xFFE08A1E, 0xFFF0A64A)
val EtapaGanado: Color get() = VerdeExito
val EtapaPerdido: Color get() = RojoError

// Nombres usados en la Semana 8 (se mantienen por compatibilidad)
val EtapaProspecto: Color get() = EtapaNuevo
val EtapaPropuesta: Color get() = EtapaCalificado
val EtapaNegociacion: Color get() = EtapaCotizado
val EtapaCerrado: Color get() = EtapaGanado
