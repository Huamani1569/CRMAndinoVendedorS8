package com.crmandino.vendedor.model

// MODELO de la aplicación: lo que usan los ViewModel y las pantallas.
// (Las entidades de Room y los DTO de la API se convierten a estos modelos en data/mapper.)

// Factor que explica el puntaje del modelo ML-01 (peso > 0 lo sube, < 0 lo baja).
data class FactorPuntaje(val factor: String?, val peso: Double?)

data class Lead(
    val id: Int,                 // identificador LOCAL (Room)
    val remoteId: Int?,          // identificador REMOTO (null si aún no llega al servidor)
    val uuid: String,            // identificador global, se genera en el celular
    val empresa: String,
    val contacto: String,
    val telefono: String?,
    val correo: String?,
    val ruc: String?,
    val direccion: String?,
    val latitud: Double?,
    val longitud: Double?,
    val valorPotencial: Double,
    val etapa: String,           // código: NUEVO, CONTACTADO, CALIFICADO, COTIZADO, GANADO, PERDIDO
    val canalOrigen: String?,    // VENDEDOR, PORTAL, CAMPANA
    val codigoSeguimiento: String?,
    val comentarioCliente: String?,
    val puntaje: Int?,           // probabilidad de cierre 0–100 (ML-01)
    val factores: List<FactorPuntaje>,
    val diasSinContacto: Int?,
    val clienteId: Int?,
    val motivoPerdida: String?,
    val vendedorId: Int,
    val vendedorNombre: String,
    val version: Int,            // última versión confirmada por el servidor
    val estadoSync: EstadoSincronizacion,
    val mensajeError: String?
) {
    val etapaVenta: EtapaVenta get() = EtapaVenta.desdeTexto(etapa)
}

// Resumen de una etapa del embudo (para el gráfico).
data class ResumenEtapa(val etapa: String, val cantidad: Int, val valor: Double)
