package com.ddcontrol.ddcontrol_android.ui.incidencias

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
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ddcontrol.ddcontrol_android.data.model.IncidenciaResponse
import com.ddcontrol.ddcontrol_android.ui.theme.Green
import com.ddcontrol.ddcontrol_android.ui.theme.Navy
import com.ddcontrol.ddcontrol_android.ui.theme.Red
import com.ddcontrol.ddcontrol_android.ui.theme.RedBg
import com.ddcontrol.ddcontrol_android.ui.theme.Surface
import com.ddcontrol.ddcontrol_android.ui.theme.TextLabel
import com.ddcontrol.ddcontrol_android.ui.theme.TextMuted
import com.ddcontrol.ddcontrol_android.ui.theme.Yellow
import com.ddcontrol.ddcontrol_android.ui.theme.YellowBg
import com.ddcontrol.ddcontrol_android.util.SessionManager
import kotlinx.coroutines.delay

@Composable
fun IncidenciasScreen(
    session: SessionManager,
    vm: IncidenciasViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    var mostrarFormulario by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.cargar(session.getUserId()) }

    LaunchedEffect(state.mensajeExito) {
        if (state.mensajeExito != null) {
            delay(2000)
            mostrarFormulario = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text("MIS INCIDENCIAS", color = TextLabel, fontSize = 11.sp)
            Button(
                onClick = { mostrarFormulario = !mostrarFormulario },
                colors  = ButtonDefaults.buttonColors(containerColor = Blue)
            ) {
                Text(if (mostrarFormulario) "Cancelar" else "+ Nueva", fontSize = 12.sp)
            }
        }

        if (mostrarFormulario) {
            NuevaIncidenciaForm(
                creando      = state.creando,
                error        = state.error,
                mensajeExito = state.mensajeExito,
                onEnviar     = { tipo, desc -> vm.crear(session.getUserId(), tipo, desc) }
            )
        }

        if (state.loading) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.incidencias.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No tienes incidencias registradas", color = TextMuted, fontSize = 15.sp)
            }
        } else {
            LazyColumn(
                contentPadding      = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(state.incidencias) { i -> IncidenciaItem(i) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaIncidenciaForm(
    creando:      Boolean,
    error:        String?,
    mensajeExito: String?,
    onEnviar:     (tipo: String, descripcion: String) -> Unit
) {
    val tipos = listOf("GPS", "retraso", "olvido", "otro")
    var tipoSeleccionado by remember { mutableStateOf(tipos[0]) }
    var descripcion      by remember { mutableStateOf("") }
    var expandido        by remember { mutableStateOf(false) }

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape     = RoundedCornerShape(10.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Nueva incidencia",
                fontWeight = FontWeight.Bold,
                color      = Navy,
                fontSize   = 14.sp
            )

            ExposedDropdownMenuBox(
                expanded         = expandido,
                onExpandedChange = { expandido = !expandido }
            ) {
                OutlinedTextField(
                    value         = tipoSeleccionado,
                    onValueChange = {},
                    readOnly      = true,
                    label         = { Text("Tipo") },
                    trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
                    modifier      = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded         = expandido,
                    onDismissRequest = { expandido = false }
                ) {
                    tipos.forEach { tipo ->
                        DropdownMenuItem(
                            text    = { Text(tipo) },
                            onClick = { tipoSeleccionado = tipo; expandido = false }
                        )
                    }
                }
            }

            OutlinedTextField(
                value         = descripcion,
                onValueChange = { descripcion = it },
                label         = { Text("Descripción (opcional)") },
                minLines      = 2,
                modifier      = Modifier.fillMaxWidth()
            )

            if (!error.isNullOrBlank()) {
                Text(error, color = Red, fontSize = 12.sp)
            }
            if (!mensajeExito.isNullOrBlank()) {
                Text(mensajeExito, color = Green, fontSize = 12.sp)
            }

            Button(
                onClick  = { onEnviar(tipoSeleccionado, descripcion) },
                enabled  = !creando,
                modifier = Modifier.fillMaxWidth(),
                colors   = ButtonDefaults.buttonColors(containerColor = Blue)
            ) {
                if (creando) {
                    CircularProgressIndicator(
                        color       = Color.White,
                        modifier    = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Enviar incidencia")
                }
            }
        }
    }
}

@Composable
fun IncidenciaItem(i: IncidenciaResponse) {
    val badgeColor = when (i.tipo.lowercase()) {
        "gps"     -> RedBg    to Red
        "retraso" -> YellowBg to Yellow
        "olvido"  -> YellowBg to Yellow
        else      -> Color(0xFFEEF2FF) to Color(0xFF3D5AFE)
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(10.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Surface(shape = RoundedCornerShape(20.dp), color = badgeColor.first) {
                    Text(
                        i.tipo,
                        color      = badgeColor.second,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier   = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
                Text(
                    if (i.resuelta) "Resuelta" else "Abierta",
                    color      = if (i.resuelta) Green else Red,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(8.dp))
            if (!i.descripcion.isNullOrBlank()) {
                Text(i.descripcion, color = TextMuted, fontSize = 13.sp)
                Spacer(Modifier.height(4.dp))
            }
            Text(
                i.fecha?.take(16)?.replace("T", " ") ?: "—",
                color    = TextLabel,
                fontSize = 11.sp
            )
        }
    }
}