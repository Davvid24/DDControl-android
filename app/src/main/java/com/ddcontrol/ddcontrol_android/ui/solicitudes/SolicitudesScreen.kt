package com.ddcontrol.ddcontrol_android.ui.solicitudes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
    ) {
        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(0.dp),
            colors    = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Box(Modifier.padding(16.dp)) {
                Button(
                    onClick  = { showDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape  = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("+ Nueva solicitud", fontWeight = FontWeight.Bold)
                }
            }
        }

        Text("MIS SOLICITUDES", color = TextLabel, fontSize = 11.sp, modifier = Modifier.padding(16.dp))

        if (state.loading) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
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

@Composable
fun SolicitudItem(s: SolicitudResponse) {
    val (badgeColor, textColor) = when (s.estado.lowercase()) {
        "aprobada"  -> GreenBg to Green
        "denegada"  -> RedBg   to Red
        else        -> YellowBg to Yellow
    }
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(10.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
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
                "${s.fechaInicio} → ${s.fechaFin}",
                color = TextMuted,
                fontSize = 12.sp
            )
            if (!s.motivo.isNullOrBlank()) {
                Text(s.motivo, color = TextMuted, fontSize = 12.sp)
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
    var tipo   by remember { mutableStateOf("VACACIONES") }
    var inicio by remember { mutableStateOf("") }
    var fin    by remember { mutableStateOf("") }
    var motivo by remember { mutableStateOf("") }
    val tipos  = listOf("VACACIONES", "BAJA", "PERMISO", "OTRO")
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title  = { Text("Nueva solicitud", fontWeight = FontWeight.Bold) },
        text   = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Tipo
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value         = tipo,
                        onValueChange = {},
                        readOnly      = true,
                        label         = { Text("Tipo") },
                        trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier      = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        tipos.forEach { t ->
                            DropdownMenuItem(
                                text    = { Text(t) },
                                onClick = { tipo = t; expanded = false }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = inicio, onValueChange = { inicio = it },
                    label = { Text("Fecha inicio (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                OutlinedTextField(
                    value = fin, onValueChange = { fin = it },
                    label = { Text("Fecha fin (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                OutlinedTextField(
                    value = motivo, onValueChange = { motivo = it },
                    label = { Text("Motivo (opcional)") },
                    modifier = Modifier.fillMaxWidth(), minLines = 2
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(tipo, inicio, fin, motivo) }) {
                Text("Enviar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}