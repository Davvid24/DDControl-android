package com.ddcontrol.ddcontrol_android.ui.navigation

import SessionManager
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.ddcontrol.ddcontrol_android.data.api.RetrofitClient
import com.ddcontrol.ddcontrol_android.ui.dashboard.DashboardScreen
import com.ddcontrol.ddcontrol_android.ui.fichajes.FichajesScreen
import com.ddcontrol.ddcontrol_android.ui.incidencias.IncidenciasScreen
import com.ddcontrol.ddcontrol_android.ui.login.LoginScreen
import com.ddcontrol.ddcontrol_android.ui.solicitudes.SolicitudesScreen
import com.ddcontrol.ddcontrol_android.util.LanguageManager
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val labelKey: String, val icon: ImageVector) {
    object Dashboard   : Screen("dashboard",   "nav.inicio",      Icons.Default.Home)
    object Fichajes    : Screen("fichajes",    "nav.fichajes",    Icons.Default.AccessTime)
    object Solicitudes : Screen("solicitudes", "nav.solicitudes", Icons.Default.Description)
    object Incidencias : Screen("incidencias", "nav.incidencias", Icons.Default.Warning)
}

val bottomScreens = listOf(
    Screen.Dashboard,
    Screen.Fichajes,
    Screen.Solicitudes,
    Screen.Incidencias
)

@Composable
fun AppNavigation(session: SessionManager) {
    val navController = rememberNavController()
    val isLoggedIn by SessionManager.isLoggedInFlow.collectAsState()

    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController    = navController,
        startDestination = if (session.isLoggedIn()) "home" else "login"
    ) {
        composable("login") {
            LoginScreen(
                session = session,
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            HomeScaffold(session = session, onLogout = {
                val userId = session.getUserId()
                val jwt    = session.getToken()
                if (userId != -1 && jwt != null) {
                    RetrofitClient.setToken(jwt)
                    FirebaseMessaging.getInstance().token.addOnSuccessListener { fcmToken ->
                        CoroutineScope(Dispatchers.IO).launch {
                            try { RetrofitClient.instance.removeDevice(mapOf("fcmToken" to fcmToken)) }
                            catch (_: Exception) {}
                        }
                    }
                }
                session.clearSession()
            })
        }
    }
}

@Composable
fun HomeScaffold(session: SessionManager, onLogout: () -> Unit) {
    val navController = rememberNavController()
    val navBackStack  by navController.currentBackStackEntryAsState()
    val currentDest   = navBackStack?.destination
    val lang          by LanguageManager.lang.collectAsState()

    Scaffold(
        bottomBar = {
            key(lang) {
                NavigationBar {
                    bottomScreens.forEach { screen ->
                        NavigationBarItem(
                            selected = currentDest?.hierarchy?.any { it.route == screen.route } == true,
                            onClick  = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            },
                            icon  = { Icon(screen.icon, contentDescription = LanguageManager.t(screen.labelKey)) },
                            label = { Text(LanguageManager.t(screen.labelKey)) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        key(lang) {
            NavHost(
                navController    = navController,
                startDestination = Screen.Dashboard.route,
                modifier         = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Dashboard.route)   { DashboardScreen(session, onLogout) }
                composable(Screen.Fichajes.route)    { FichajesScreen(session) }
                composable(Screen.Solicitudes.route) { SolicitudesScreen(session) }
                composable(Screen.Incidencias.route) { IncidenciasScreen(session) }
            }
        }
    }
}