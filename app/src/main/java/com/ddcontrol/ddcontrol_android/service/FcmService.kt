package com.ddcontrol.ddcontrol_android.service

import android.util.Log
import com.ddcontrol.ddcontrol_android.data.api.RetrofitClient
import com.ddcontrol.ddcontrol_android.util.NotificationHelper
import com.ddcontrol.ddcontrol_android.util.SessionManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FcmService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d("FCM", "LLEGÓ NOTIFICACIÓN");
        val title = message.notification?.title ?: message.data["title"] ?: return
        val body  = message.notification?.body  ?: message.data["body"]  ?: return
        val type  = message.data["type"] ?: "general"

        val channelId = when (type) {
            "solicitud" -> NotificationHelper.CHANNEL_SOLICITUD
            "turno"     -> NotificationHelper.CHANNEL_TURNO
            "fichaje"   -> NotificationHelper.CHANNEL_FICHAJE
            else        -> NotificationHelper.CHANNEL_SOLICITUD
        }

        NotificationHelper.show(
            context   = applicationContext,
            id        = System.currentTimeMillis().toInt(),
            channelId = channelId,
            title     = title,
            message   = body
        )
    }

    override fun onNewToken(token: String) {
        val session = SessionManager(applicationContext)
        val userId  = session.getUserId()
        if (userId == -1) return

        RetrofitClient.setToken(session.getToken())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                RetrofitClient.instance.registerDevice(
                    userId,
                    mapOf("fcmToken" to token)
                )
            } catch (_: Exception) {}
        }
    }
}