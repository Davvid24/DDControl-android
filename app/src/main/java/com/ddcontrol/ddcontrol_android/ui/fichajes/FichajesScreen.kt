package com.ddcontrol.ddcontrol_android.ui.fichajes

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ddcontrol.ddcontrol_android.data.model.FichajeResponse
import com.ddcontrol.ddcontrol_android.ui.theme.*
import com.ddcontrol.ddcontrol_android.util.SessionManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FichajesScreen(
    session: SessionManager
) {
    val context = LocalContext.current
    val vm: FichajesViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return FichajesViewModel(context.applicationContext) as T
            }
        }
    )

    val state by vm.state.collectAsState()
    var latLon by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    val cancellationTokenSource = remember { CancellationTokenSource() }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { loc ->
                val lat = loc?.latitude ?: 0.0
                val lon = loc?.longitude ?: 0.0
                latLon = Pair(lat, lon)
                vm.fichar(session.getUserId(), session.getEmpresaId(), lat, lon)
            }.addOnFailureListener {
                vm.fichar(session.getUserId(), session.getEmpresaId(), 0.0, 0.0)
            }
        }
    }

    LaunchedEffect(Unit) {
        vm.cargar(session.getUserId())
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { loc ->
            if (loc != null) latLon = Pair(loc.latitude, loc.longitude)
        }
    }

    DisposableEffect(Unit) {
        onDispose { cancellationTokenSource.cancel() }
    }

    if (state.aviso != null) {
        AlertDialog(
            onDismissRequest = { vm.descartarAviso() },
            title = { Text("Aviso", fontWeight = FontWeight.Bold) },
            text = { Text(state.aviso!!) },
            confirmButton = {
                Button(onClick = {
                    val (lat, lon) = latLon ?: Pair(0.0, 0.0)
                    vm.confirmarFichajeConAviso(session.getUserId(), session.getEmpresaId(), lat, lon)
                }) { Text("Fichar igualmente") }
            },
            dismissButton = {
                TextButton(onClick = { vm.descartarAviso() }) { Text("Cancelar") }
            }
        )
    }

    PullToRefreshBox(
        isRefreshing = state.loadingLista,
        onRefresh = { vm.cargar(session.getUserId()) },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Surface)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                    if (state.turnoNombre != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                            Text(state.turnoNombre!!, color = Primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (state.turnoHoraEntrada != null && state.turnoHoraSalida != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Surface, RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            HorarioItem("Entrada", state.turnoHoraEntrada!!.take(5))
                            HorarioItem("Salida", state.turnoHoraSalida!!.take(5))
                            HorarioItem(
                                "Hoy",
                                if (state.esDiaTurno) "Día laboral" else "No laboral",
                                if (state.esDiaTurno) Green else TextMuted
                            )
                        }
                    }

                    Button(
                        onClick = {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        enabled = !state.loading && !state.enPausa,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when {
                                state.enPausa -> TextMuted
                                state.siguienteTipo == "entrada" -> Green
                                else -> Red
                            }
                        )
                    ) {
                        if (state.loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                if (state.siguienteTipo == "entrada") Icons.Default.Login else Icons.Default.Logout,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                when {
                                    state.enPausa -> "En pausa — reanuda primero"
                                    state.siguienteTipo == "entrada" -> "Registrar entrada"
                                    else -> "Registrar salida"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (state.puedePonerPausa || state.enPausa) {
                        val colorPausa = if (state.enPausa) Color(0xFFE65100) else Color(0xFFF59E0B)
                        val colorPausaBg = if (state.enPausa) Color(0xFFFFF3E0) else Color(0xFFFFFDE7)

                        OutlinedButton(
                            onClick = {
                                val (lat, lon) = latLon ?: Pair(0.0, 0.0)
                                vm.ficharPausa(session.getUserId(), lat, lon)
                            },
                            enabled = !state.loadingPausa && !state.loading,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, colorPausa),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = colorPausaBg)
                        ) {
                            if (state.loadingPausa) {
                                CircularProgressIndicator(color = colorPausa, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = colorPausa, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    if (state.enPausa) "Finalizar pausa" else "Iniciar pausa",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colorPausa
                                )
                            }
                        }
                    }

                    if (state.mensaje != null) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Green, modifier = Modifier.size(16.dp))
                            Text(state.mensaje!!, color = Green, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (state.error != null) {
                        Text(state.error!!, color = Red, fontSize = 13.sp)
                    }

                    if (latLon != null) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.GpsFixed, contentDescription = null, tint = Green, modifier = Modifier.size(14.dp))
                            Text(
                                "GPS: ${"%.4f".format(latLon!!.first)}, ${"%.4f".format(latLon!!.second)}",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.GpsOff, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                            Text("Sin ubicación GPS", color = TextMuted, fontSize = 11.sp)
                        }
                    }
                }
            }

            Text(
                "FICHAJES DE HOY",
                color = TextLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            if (state.fichajesHoy.isEmpty()) {
                Box(
                    Modifier.fillMaxWidth().padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Sin fichajes hoy", color = TextMuted, fontSize = 14.sp, textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.fichajesHoy) { fichaje -> FichajeItemHoy(fichaje) }
                }
            }
        }
    }
}

@Composable
private fun HorarioItem(label: String, valor: String, colorValor: Color = Navy) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 11.sp, color = TextLabel, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(2.dp))
        Text(valor, fontSize = 14.sp, color = colorValor, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FichajeItemHoy(f: FichajeResponse) {
    val hora = try {
        val instant = Instant.parse(f.timestampFicha)
        val zoned = instant.atZone(ZoneId.of("Europe/Madrid"))
        DateTimeFormatter.ofPattern("HH:mm").format(zoned)
    } catch (_: Exception) {
        f.timestampFicha?.take(5) ?: "—"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when (f.tipo) {
                    "entrada" -> GreenBg
                    "salida" -> RedBg
                    "pausa_inicio" -> Color(0xFFFFFDE7)
                    "pausa_fin" -> Color(0xFFE8F5E9)
                    else -> Surface
                },
                modifier = Modifier.width(90.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                    Text(
                        when (f.tipo) {
                            "entrada" -> "Entrada"
                            "salida" -> "Salida"
                            "pausa_inicio" -> "⏸ Pausa"
                            "pausa_fin" -> "▶ Reanuda"
                            else -> f.tipo
                        },
                        color = when (f.tipo) {
                            "entrada" -> Green
                            "salida" -> Red
                            "pausa_inicio" -> Color(0xFFF59E0B)
                            "pausa_fin" -> Color(0xFF2E7D32)
                            else -> TextMuted
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(hora, color = Navy, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(
                    if (f.dentroDeRadio == true) Icons.Default.GpsFixed else Icons.Default.GpsOff,
                    contentDescription = null,
                    tint = if (f.dentroDeRadio == true) Green else Red,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    if (f.dentroDeRadio == true) "Dentro" else "Fuera",
                    color = if (f.dentroDeRadio == true) Green else Red,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}