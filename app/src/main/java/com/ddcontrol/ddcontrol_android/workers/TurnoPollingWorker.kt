package com.ddcontrol.ddcontrol_android.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ddcontrol.ddcontrol_android.data.api.RetrofitClient
import com.ddcontrol.ddcontrol_android.util.NotificationHelper
import com.ddcontrol.ddcontrol_android.util.SessionManager
import java.time.LocalDate

class TurnoPollingWorker(
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
            val hoy = LocalDate.now()
            val resp = RetrofitClient.instance.getCalendario(userId, hoy.year, hoy.monthValue)
            if (!resp.isSuccessful || resp.body() == null) return Result.success()

            val turnoActual = resp.body()!!.turno?.nombre ?: "Sin turno"
            val turnoAnterior = prefs.getString("turno_nombre_$userId", null)

            if (turnoAnterior != null && turnoAnterior != turnoActual) {
                NotificationHelper.show(
                    applicationContext,
                    id = 3001,
                    channelId = NotificationHelper.CHANNEL_TURNO,
                    title = "Tu turno ha cambiado",
                    message = "Se te ha asignado el turno: $turnoActual"
                )
            }

            prefs.edit().putString("turno_nombre_$userId", turnoActual).apply()

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}