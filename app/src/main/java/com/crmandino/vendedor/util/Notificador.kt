package com.crmandino.vendedor.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/**
 * Muestra como NOTIFICACIÓN DEL SISTEMA las notificaciones que el servidor guarda para el usuario
 * (tabla notificaciones). La app las consulta cada cierto tiempo (GET /api/notificaciones);
 * con Firebase Cloud Messaging llegarían al instante, pero el flujo para el usuario es el mismo.
 */
object Notificador {
    private const val CANAL = "crm_andino_avisos"

    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(CANAL, "Avisos de CRM Andino", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Cotizaciones, tickets, ofertas y alertas"
            }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
        }
    }

    fun puedeNotificar(context: Context): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun mostrar(context: Context, id: Int, titulo: String, cuerpo: String) {
        if (!puedeNotificar(context)) return
        val abrir = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("abrir_notificaciones", true)
        }
        val pendiente = abrir?.let {
            PendingIntent.getActivity(context, id, it, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
        val notificacion = NotificationCompat.Builder(context, CANAL)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle(titulo)
            .setContentText(cuerpo)
            .setStyle(NotificationCompat.BigTextStyle().bigText(cuerpo))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .apply { if (pendiente != null) setContentIntent(pendiente) }
            .build()
        try {
            NotificationManagerCompat.from(context).notify(id, notificacion)
        } catch (e: SecurityException) {
            // El usuario quitó el permiso de notificaciones: se ignora
        }
    }
}
