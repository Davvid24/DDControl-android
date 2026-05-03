package com.ddcontrol.ddcontrol_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ddcontrol.ddcontrol_android.ui.navigation.AppNavigation
import com.ddcontrol.ddcontrol_android.ui.theme.DDControlTheme
import com.ddcontrol.ddcontrol_android.util.SessionManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val session = SessionManager(this)
        setContent {
            DDControlTheme {
                AppNavigation(session)
            }
        }
    }
}