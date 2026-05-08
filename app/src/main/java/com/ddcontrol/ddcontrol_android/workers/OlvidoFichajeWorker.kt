package com.ddcontrol.ddcontrol_android.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ddcontrol.ddcontrol_android.data.api.RetrofitClient
import com.ddcontrol.ddcontrol_android.util.NotificationHelper
import com.ddcontrol.ddcontrol_android.util.SessionManager
import java.time.Instant
import java.time.LocalDate

class OlvidoFichajeWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val session = SessionManager(applicationContext)
        val userId = session.getUserId()
        if (userId == -1) return Result.success()

        RetrofitClient.setToken(session.getToken())

        return try {
            val resp = RetrofitClient.instance.getFichajesByUsuario(userId)
            if (!resp.isSuccessful || resp.body() == null) return Result.success()

            val hoy = LocalDate.now().toString()
            val fichajesHoy = resp.body()!!
                .filter { it.timestampFicha?.startsWith(hoy) == true }
                .sortedByDescending { it.timestampFicha }

            val ultimo = fichajesHoy.firstOrNull() ?: return Result.success()

            if (ultimo.tipo == "entrada") {
                val entradaInstant = Instant.parse(ultimo.timestampFicha)
                val horasTranscurridas = java.time.Duration.between(
                    entradaInstant, Instant.now()
                ).toHours()

                if (horasTranscurridas >= 8) {
                    NotificationHelper.show(
                        applicationContext,
                        id = 1002,
                        channelId = NotificationHelper.CHANNEL_FICHAJE,
                        title = "¿Olvidaste fichar la salida?",
                        message = "Llevas más de ${horasTranscurridas}h fichado. Recuerda registrar tu salida."
                    )
                }
            }

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}