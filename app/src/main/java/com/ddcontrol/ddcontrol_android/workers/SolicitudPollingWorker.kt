package com.ddcontrol.ddcontrol_android.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ddcontrol.ddcontrol_android.data.api.RetrofitClient
import com.ddcontrol.ddcontrol_android.util.NotificationHelper
import com.ddcontrol.ddcontrol_android.util.SessionManager

class SolicitudPollingWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val session = SessionManager(applicationContext)
        val userId = session.getUserId()
        if (userId == -1) return Result.success()

        RetrofitClient.setToken(session.getToken())

        val prefs = applicationContext.getSharedPreferences("polling_prefs", Context.MODE_PRIVATE)

        return try {
            val resp = RetrofitClient.instance.getSolicitudesByUsuario(userId)
            if (!resp.isSuccessful || resp.body() == null) return Result.success()

            val solicitudes = resp.body()!!

            solicitudes.forEach { solicitud ->
                val key = "solicitud_estado_${solicitud.id}"
                val estadoAnterior = prefs.getString(key, null)
                val estadoActual = solicitud.estado.lowercase()

                if (estadoAnterior != null && estadoAnterior != estadoActual) {
                    val (titulo, mensaje) = when (estadoActual) {
                        "aprobada" -> Pair(
                            "Solicitud aprobada",
                            "Tu solicitud de ${solicitud.tipo} del ${solicitud.fechaInicio} al ${solicitud.fechaFin} ha sido aprobada."
                        )
                        "rechazada" -> Pair(
                            "Solicitud rechazada",
                            "Tu solicitud de ${solicitud.tipo} del ${solicitud.fechaInicio} al ${solicitud.fechaFin} ha sido rechazada."
                        )
                        else -> null to null
                    }
                    if (titulo != null && mensaje != null) {
                        NotificationHelper.show(
                            applicationContext,
                            id = 2000 + solicitud.id,
                            channelId = NotificationHelper.CHANNEL_SOLICITUD,
                            title = titulo,
                            message = mensaje
                        )
                    }
                }

                if (estadoAnterior == null || estadoAnterior != estadoActual) {
                    prefs.edit().putString(key, estadoActual).apply()
                }
            }

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}