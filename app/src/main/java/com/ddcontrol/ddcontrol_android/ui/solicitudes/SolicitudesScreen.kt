package com.ddcontrol.ddcontrol_android.ui.solicitudes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ddcontrol.ddcontrol_android.data.model.SolicitudResponse
import com.ddcontrol.ddcontrol_android.ui.theme.*
import com.ddcontrol.ddcontrol_android.util.SessionManager
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudesScreen(
    session: SessionManager,
    vm: SolicitudesViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.cargar(session.getUserId()) }

    if (showDialog) {
        NuevaSolicitudDialog(
            onDismiss = { showDialog = false },
            onConfirm = { tipo, inicio, fin, motivo ->
                vm.crear(session.getUserId(), tipo, inicio, fin, motivo)
                showDialog = false
            }
        )
    }

    PullToRefreshBox(
        isRefreshing = state.loading,
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
                Box(Modifier.padding(16.dp)) {
                    Button(
                        onClick = { showDialog = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text("+ Nueva solicitud", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text(
                "MIS SOLICITUDES",
                color = TextLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            if (state.solicitudes.isEmpty() && !state.loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No tienes solicitudes registradas", color = TextMuted, fontSize = 15.sp)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(state.solicitudes) { s -> SolicitudItem(s) }
                }
            }

            if (state.error != null) {
                Text(state.error!!, color = Red, modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaSolicitudDialog(
    onDismiss: () -> Unit,
    onConfirm: (tipo: String, inicio: String, fin: String, motivo: String) -> Unit
) {
    val tipos = listOf("VACACIONES", "BAJA", "PERMISO", "OTRO")
    var tipoSeleccionado by remember { mutableStateOf(tipos[0]) }
    var motivo by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val hoy = LocalDate.now()
    val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val fmtDisplay = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    var fechaInicio by remember { mutableStateOf<LocalDate?>(null) }
    var fechaFin by remember { mutableStateOf<LocalDate?>(null) }

    var showPickerInicio by remember { mutableStateOf(false) }
    var showPickerFin by remember { mutableStateOf(false) }

    val pickerStateInicio = rememberDatePickerState(
        initialSelectedDateMillis = hoy.plusDays(1)
            .atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val fecha = Instant.ofEpochMilli(utcTimeMillis)
                    .atZone(ZoneId.of("UTC")).toLocalDate()
                return !fecha.isBefore(hoy)
            }
        }
    )

    val pickerStateFin = rememberDatePickerState(
        initialSelectedDateMillis = hoy.plusDays(1)
            .atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val fecha = Instant.ofEpochMilli(utcTimeMillis)
                    .atZone(ZoneId.of("UTC")).toLocalDate()
                val minFecha = fechaInicio ?: hoy
                return !fecha.isBefore(minFecha)
            }
        }
    )

    if (showPickerInicio) {
        DatePickerDialog(
            onDismissRequest = { showPickerInicio = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerStateInicio.selectedDateMillis?.let { millis ->
                        val fecha = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.of("UTC")).toLocalDate()
                        fechaInicio = fecha
                        if (fechaFin != null && fechaFin!!.isBefore(fecha)) fechaFin = null
                    }
                    showPickerInicio = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showPickerInicio = false }) { Text("Cancelar") }
            }
        ) { DatePicker(state = pickerStateInicio) }
    }

    if (showPickerFin) {
        DatePickerDialog(
            onDismissRequest = { showPickerFin = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerStateFin.selectedDateMillis?.let { millis ->
                        val fecha = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.of("UTC")).toLocalDate()
                        fechaFin = fecha
                    }
                    showPickerFin = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showPickerFin = false }) { Text("Cancelar") }
            }
        ) { DatePicker(state = pickerStateFin) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva solicitud", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = tipoSeleccionado,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        tipos.forEach { tipo ->
                            DropdownMenuItem(
                                text = { Text(tipo) },
                                onClick = { tipoSeleccionado = tipo; expanded = false }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = fechaInicio?.format(fmtDisplay) ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fecha inicio") },
                    placeholder = { Text("Selecciona fecha") },
                    trailingIcon = {
                        IconButton(onClick = { showPickerInicio = true }) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Seleccionar fecha inicio",
                                tint = Primary
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = fechaFin?.format(fmtDisplay) ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fecha fin") },
                    placeholder = { Text("Selecciona fecha") },
                    trailingIcon = {
                        IconButton(onClick = {
                            if (fechaInicio == null) errorMsg = "Selecciona primero la fecha de inicio"
                            else showPickerFin = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Seleccionar fecha fin",
                                tint = if (fechaInicio == null) TextLabel else Primary
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = motivo,
                    onValueChange = { motivo = it },
                    label = { Text("Motivo (opcional)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMsg != null) {
                    Text(errorMsg!!, color = Red, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                when {
                    fechaInicio == null -> errorMsg = "Selecciona la fecha de inicio"
                    fechaFin == null -> errorMsg = "Selecciona la fecha de fin"
                    fechaFin!!.isBefore(fechaInicio) -> errorMsg = "La fecha de fin no puede ser anterior a la de inicio"
                    else -> {
                        errorMsg = null
                        onConfirm(tipoSeleccionado, fechaInicio!!.format(fmt), fechaFin!!.format(fmt), motivo)
                    }
                }
            }) { Text("Enviar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun SolicitudItem(s: SolicitudResponse) {
    val fmtDisplay = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun formatFecha(iso: String?): String {
        if (iso == null) return "—"
        return try { LocalDate.parse(iso).format(fmtDisplay) } catch (_: Exception) { iso }
    }

    val (badgeColor, textColor) = when (s.estado.lowercase()) {
        "aprobada" -> GreenBg to Green
        "denegada" -> RedBg to Red
        else -> YellowBg to Yellow
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(s.tipo, color = Navy, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Surface(shape = RoundedCornerShape(20.dp), color = badgeColor) {
                    Text(
                        s.estado.replaceFirstChar { it.uppercase() },
                        color = textColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "${formatFecha(s.fechaInicio?.toString())} → ${formatFecha(s.fechaFin?.toString())}",
                color = TextMuted,
                fontSize = 12.sp
            )
            if (!s.motivo.isNullOrBlank()) {
                Text(s.motivo, color = TextMuted, fontSize = 12.sp)
            }
            if (!s.comentarioAdmin.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Respuesta: ${s.comentarioAdmin}",
                    color = TextLabel,
                    fontSize = 11.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}