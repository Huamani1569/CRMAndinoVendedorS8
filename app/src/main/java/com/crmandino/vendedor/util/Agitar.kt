package com.crmandino.vendedor.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import kotlin.math.sqrt

/**
 * SENSOR (Semana 13): ACELERÓMETRO. Si el usuario agita el celular, se ejecuta [alAgitar]
 * (por ejemplo, sincronizar). Se calcula la fuerza G = √(x² + y² + z²) / 9.81; más de 2.5 G cuenta
 * como sacudida, con una pausa de 2 s entre una y otra. El sensor solo escucha mientras la pantalla está visible.
 */
@Composable
fun DetectorAgitar(alAgitar: () -> Unit) {
    val context = LocalContext.current
    val accion = rememberUpdatedState(alAgitar)
    DisposableEffect(Unit) {
        val gestor = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val acelerometro = gestor?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        var ultima = 0L
        val oyente = object : SensorEventListener {
            override fun onSensorChanged(e: SensorEvent) {
                val (x, y, z) = Triple(e.values[0], e.values[1], e.values[2])
                val fuerzaG = sqrt(x * x + y * y + z * z) / SensorManager.GRAVITY_EARTH
                val ahora = System.currentTimeMillis()
                if (fuerzaG > 2.5f && ahora - ultima > 2_000) {
                    ultima = ahora
                    accion.value()
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        if (acelerometro != null) gestor?.registerListener(oyente, acelerometro, SensorManager.SENSOR_DELAY_UI)
        onDispose { gestor?.unregisterListener(oyente) }
    }
}
