package com.ddcontrol.ddcontrol_android.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.ddcontrol.ddcontrol_android.R

object NotificationHelper {

    const val CHANNEL_TURNO = "channel_turno"
    const val CHANNEL_FICHAJE = "channel_fichaje"
    const val CHANNEL_SOLICITUD = "channel_solicitud"

    fun createChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        listOf(
            NotificationChannel(CHANNEL_TURNO, "Turno", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Avisos sobre tu turno de trabajo"
            },
            NotificationChannel(CHANNEL_FICHAJE, "Fichajes", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Avisos sobre tus fichajes"
            },
            NotificationChannel(CHANNEL_SOLICITUD, "Solicitudes", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Cambios en tus solicitudes"
            }
        ).forEach { manager.createNotificationChannel(it) }
    }

    fun show(
        context: Context,
        id: Int,
        channelId: String,
        title: String,
        message: String
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        manager.notify(id, notification)
    }
}