package com.ddcontrol.ddcontrol_android

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.work.*
import com.ddcontrol.ddcontrol_android.data.api.RetrofitClient
import com.ddcontrol.ddcontrol_android.ui.navigation.AppNavigation
import com.ddcontrol.ddcontrol_android.ui.theme.DDControlTheme
import com.ddcontrol.ddcontrol_android.util.NotificationHelper
import com.ddcontrol.ddcontrol_android.util.SessionManager
import com.ddcontrol.ddcontrol_android.workers.*
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) programarWorkers()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val session = SessionManager(this)
        RetrofitClient.setToken(session.getToken())
        NotificationHelper.createChannels(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> programarWorkers()
                else -> requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            programarWorkers()
        }

        setContent {
            DDControlTheme {
                AppNavigation(session)
            }
        }
    }

    private fun programarWorkers() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workManager = WorkManager.getInstance(this)

        workManager.enqueueUniquePeriodicWork(
            "turno_reminder",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<TurnoReminderWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()
        )

        workManager.enqueueUniquePeriodicWork(
            "olvido_fichaje",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<OlvidoFichajeWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()
        )

        workManager.enqueueUniquePeriodicWork(
            "solicitud_polling",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<SolicitudPollingWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()
        )

        workManager.enqueueUniquePeriodicWork(
            "turno_polling",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<TurnoPollingWorker>(30, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()
        )
    }
}