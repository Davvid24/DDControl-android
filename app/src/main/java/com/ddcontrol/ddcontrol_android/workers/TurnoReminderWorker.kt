package com.ddcontrol.ddcontrol_android.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ddcontrol.ddcontrol_android.data.api.RetrofitClient
import com.ddcontrol.ddcontrol_android.util.NotificationHelper
import com.ddcontrol.ddcontrol_android.util.SessionManager
import java.time.LocalDate
import java.time.LocalTime

class TurnoReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val session = SessionManager(applicationContext)
        val userId = session.getUserId()
        if (userId == -1) return Result.success()

        RetrofitClient.setToken(session.getToken())

        return try {
            val hoy = LocalDate.now()
            val resp = RetrofitClient.instance.getCalendario(userId, hoy.year, hoy.monthValue)
            if (!resp.isSuccessful || resp.body() == null) return Result.success()

            val body = resp.body()!!
            val diaHoy = body.dias.find { it.fecha == hoy.toString() } ?: return Result.success()

            if (!diaHoy.esDiaTurno) return Result.success()

            val horaEntrada = body.turno?.horaEntrada ?: return Result.success()
            val entrada = LocalTime.parse(horaEntrada)
            val ahora = LocalTime.now()
            val minutosRestantes = java.time.Duration.between(ahora, entrada).toMinutes()

            if (minutosRestantes in 0..15) {
                NotificationHelper.show(
                    applicationContext,
                    id = 1001,
                    channelId = NotificationHelper.CHANNEL_TURNO,
                    title = "Tu turno empieza pronto",
                    message = "Tienes que fichar a las ${horaEntrada.take(5)}."
                )
            }

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}