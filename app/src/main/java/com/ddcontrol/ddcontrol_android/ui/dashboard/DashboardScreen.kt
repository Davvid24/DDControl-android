package com.ddcontrol.ddcontrol_android.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ddcontrol.ddcontrol_android.ui.theme.*
import com.ddcontrol.ddcontrol_android.util.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    session: SessionManager,
    onLogout: () -> Unit,
    vm: DashboardViewModel = viewModel()
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) {
        vm.load(session.getUserId(), session.getEmpresaId())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("D&D Control", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Cerrar sesión", tint = Red)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Navy
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Surface)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DashCard {
                Column {
                    Text("Hola,", color = TextMuted, fontSize = 14.sp)
                    Text(
                        session.getNombre() ?: "—",
                        color = Navy,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        state.nombreTurno ?: "Sin turno asignado",
                        color = Primary,
                        fontSize = 13.sp
                    )
                }
            }

            DashCard {
                Column {
                    Text(
                        "ESTADO ACTUAL",
                        color = TextLabel,
                        fontSize = 11.sp,
                        letterSpacing = 0.08.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        state.estadoFichaje,
                        color = Navy,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (state.ultimoFichaje != null) {
                        Text(state.ultimoFichaje!!, color = TextMuted, fontSize = 13.sp)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    value    = state.fichajesHoy.toString(),
                    label    = "Fichajes hoy",
                    color    = Primary
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    value    = state.solicitudesPendientes.toString(),
                    label    = "Solicitudes",
                    color    = Yellow
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    value    = state.incidenciasAbiertas.toString(),
                    label    = "Incidencias",
                    color    = Red
                )
            }

            if (state.loading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun DashCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(Modifier.padding(20.dp)) { content() }
    }
}

@Composable
fun StatCard(modifier: Modifier, value: String, label: String, color: Color) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(value, color = color, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(label, color = TextMuted, fontSize = 12.sp)
        }
    }
}