package com.ddcontrol.ddcontrol_android.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ddcontrol.ddcontrol_android.data.model.DiaCalendario
import com.ddcontrol.ddcontrol_android.ui.theme.*
import com.ddcontrol.ddcontrol_android.util.SessionManager
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    session: SessionManager,
    onLogout: () -> Unit,
    vm: DashboardViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    var vistaCalendario by remember { mutableStateOf(true) }
    var mesActual by remember { mutableStateOf(YearMonth.now()) }

    LaunchedEffect(Unit) {
        vm.load(session.getUserId(), session.getEmpresaId())
    }

    LaunchedEffect(mesActual) {
        vm.loadCalendario(session.getUserId(), mesActual.year, mesActual.monthValue)
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
        PullToRefreshBox(
            isRefreshing = state.loading,
            onRefresh = {
                vm.load(session.getUserId(), session.getEmpresaId())
                vm.loadCalendario(session.getUserId(), mesActual.year, mesActual.monthValue)
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Surface)
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

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Mi horario", color = Navy, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Row {
                                TextButton(onClick = { vistaCalendario = true }) {
                                    Text(
                                        "Mes",
                                        color = if (vistaCalendario) Primary else TextMuted,
                                        fontWeight = if (vistaCalendario) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                                TextButton(onClick = { vistaCalendario = false }) {
                                    Text(
                                        "Semana",
                                        color = if (!vistaCalendario) Primary else TextMuted,
                                        fontWeight = if (!vistaCalendario) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { mesActual = mesActual.minusMonths(1) }) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = Navy)
                            }
                            Text(
                                mesActual.month.getDisplayName(TextStyle.FULL, Locale("es"))
                                    .replaceFirstChar { it.uppercase() } + " ${mesActual.year}",
                                color = Navy,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            IconButton(onClick = { mesActual = mesActual.plusMonths(1) }) {
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Navy)
                            }
                        }

                        if (state.loadingCalendario) {
                            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        } else if (vistaCalendario) {
                            CalendarioMensual(state.diasCalendario, mesActual)
                        } else {
                            CalendarioSemanal(state.diasCalendario, state.turnoHoraEntrada, state.turnoHoraSalida)
                        }

                        if (state.turnoHoraEntrada != null) {
                            Spacer(Modifier.height(12.dp))
                            HorizontalDivider(color = Surface)
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                LeyendaItem(GreenBg, Green, "Fichado")
                                LeyendaItem(Color(0xFFFFF3E0), Color(0xFFE65100), "Pendiente")
                                LeyendaItem(Surface, TextLabel, "No laboral")
                            }
                        }
                    }
                }

                DashCard {
                    Column {
                        Text("ESTADO ACTUAL", color = TextLabel, fontSize = 11.sp, letterSpacing = 0.08.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(state.estadoFichaje, color = Navy, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        if (state.ultimoFichaje != null) {
                            Text(state.ultimoFichaje!!, color = TextMuted, fontSize = 13.sp)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(Modifier.weight(1f), state.fichajesHoy.toString(), "Fichajes hoy", Primary)
                    StatCard(Modifier.weight(1f), state.solicitudesPendientes.toString(), "Solicitudes", Yellow)
                    StatCard(Modifier.weight(1f), state.incidenciasAbiertas.toString(), "Incidencias", Red)
                }
            }
        }
    }
}

@Composable
private fun CalendarioMensual(dias: List<DiaCalendario>, mes: YearMonth) {
    val diasSemana = listOf("L", "M", "X", "J", "V", "S", "D")

    Row(Modifier.fillMaxWidth()) {
        diasSemana.forEach { d ->
            Text(
                d,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextLabel
            )
        }
    }

    Spacer(Modifier.height(4.dp))

    val primerDia = mes.atDay(1)
    val offsetInicio = (primerDia.dayOfWeek.value - 1)
    val diasMes = dias

    val celdas = mutableListOf<DiaCalendario?>()
    repeat(offsetInicio) { celdas.add(null) }
    celdas.addAll(diasMes)
    while (celdas.size % 7 != 0) celdas.add(null)

    celdas.chunked(7).forEach { semana ->
        Row(Modifier.fillMaxWidth()) {
            semana.forEach { dia ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (dia != null) {
                        val numDia = dia.fecha.split("-")[2].toIntOrNull() ?: 0
                        val esHoy = dia.fecha == LocalDate.now().toString()
                        val bgColor = when {
                            dia.tieneFichaje -> GreenBg
                            dia.esDiaTurno && !dia.tieneFichaje &&
                                    LocalDate.parse(dia.fecha).isBefore(LocalDate.now()) -> Color(0xFFFFF3E0)
                            else -> Color.Transparent
                        }

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(bgColor)
                                .then(
                                    if (esHoy) Modifier.border(2.dp, Primary, CircleShape)
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    numDia.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = if (esHoy) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        dia.tieneFichaje -> Green
                                        dia.esDiaTurno -> Navy
                                        else -> TextLabel
                                    }
                                )
                                if (dia.esDiaTurno) {
                                    Box(
                                        Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (dia.tieneFichaje) Green
                                                else if (LocalDate.parse(dia.fecha).isBefore(LocalDate.now()))
                                                    Color(0xFFE65100)
                                                else Primary
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarioSemanal(
    dias: List<DiaCalendario>,
    horaEntrada: String?,
    horaSalida: String?
) {
    val hoy = LocalDate.now()
    val inicioSemana = hoy.with(DayOfWeek.MONDAY)
    val semana = (0..6).map { inicioSemana.plusDays(it.toLong()) }

    val diasMap = dias.associateBy { it.fecha }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        semana.forEach { fecha ->
            val fechaStr = fecha.toString()
            val dia = diasMap[fechaStr]
            val esHoy = fecha == hoy
            val nombreDia = fecha.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("es"))
                .replaceFirstChar { it.uppercase() }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when {
                            esHoy -> Color(0xFFEEF6FF)
                            dia?.tieneFichaje == true -> GreenBg
                            dia?.esDiaTurno == true -> Surface
                            else -> Color.Transparent
                        }
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.width(40.dp)) {
                    Text(
                        nombreDia,
                        fontSize = 12.sp,
                        fontWeight = if (esHoy) FontWeight.Bold else FontWeight.Normal,
                        color = if (esHoy) Primary else TextMuted
                    )
                    Text(
                        fecha.dayOfMonth.toString(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (esHoy) Primary else Navy
                    )
                }

                Spacer(Modifier.width(12.dp))

                if (dia?.esDiaTurno == true) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "${horaEntrada ?: "—"} – ${horaSalida ?: "—"}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Navy
                        )
                        if (dia.tieneFichaje) {
                            Text(
                                "Entrada: ${dia.horaEntrada?.take(5) ?: "—"}  Salida: ${dia.horaSalida?.take(5) ?: "—"}",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (dia.tieneFichaje) GreenBg
                                else if (fecha.isBefore(hoy)) Color(0xFFFFF3E0)
                                else Color(0xFFEEF6FF)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            when {
                                dia.tieneFichaje -> "✓ Fichado"
                                fecha.isBefore(hoy) -> "Sin fichar"
                                fecha == hoy -> "Hoy"
                                else -> "Pendiente"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                dia.tieneFichaje -> Green
                                fecha.isBefore(hoy) -> Color(0xFFE65100)
                                else -> Primary
                            }
                        )
                    }
                } else {
                    Text("No laboral", fontSize = 13.sp, color = TextLabel)
                }
            }
        }
    }
}

@Composable
private fun LeyendaItem(bg: Color, textColor: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(bg).border(1.dp, textColor, CircleShape))
        Text(label, fontSize = 11.sp, color = TextMuted)
    }
}

@Composable
fun DashCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(Modifier.padding(20.dp)) { content() }
    }
}

@Composable
fun StatCard(modifier: Modifier, value: String, label: String, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(value, color = color, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(label, color = TextMuted, fontSize = 12.sp)
        }
    }
}