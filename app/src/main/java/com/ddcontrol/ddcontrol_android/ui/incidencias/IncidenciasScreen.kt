package com.ddcontrol.ddcontrol_android.ui.incidencias

import SessionManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.ddcontrol.ddcontrol_android.data.model.IncidenciaResponse
import com.ddcontrol.ddcontrol_android.ui.theme.*
import com.ddcontrol.ddcontrol_android.util.LanguageManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidenciasScreen(
    session: SessionManager,
    vm: IncidenciasViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    val lang  by LanguageManager.lang.collectAsState()
    fun t(key: String) = LanguageManager.t(key)

    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect("cargar") { vm.cargar(session.getUserId()) }

    LaunchedEffect("exitoEvent") {
        vm.exitoEvent.collect {
            showDialog = false
            vm.limpiarMensaje()
        }
    }

    if (showDialog) {
        NuevaIncidenciaDialog(
            creando      = state.creando,
            error        = state.error,
            mensajeExito = state.mensajeExito,
            onDismiss    = { showDialog = false },
            onEnviar     = { tipo, desc -> vm.crear(session.getUserId(), tipo, desc) }
        )
    }

    PullToRefreshBox(
        isRefreshing = state.loading,
        onRefresh    = { vm.cargar(session.getUserId()) },
        modifier     = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize().background(Surface)) {

            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(0.dp),
                colors    = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Box(Modifier.padding(16.dp)) {
                    Button(
                        onClick  = { showDialog = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(t("incidencias.nueva"), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text(
                t("incidencias.titulo"),
                color    = TextLabel,
                fontSize = 11.sp,
                modifier = Modifier.padding(16.dp)
            )

            if (state.incidencias.isEmpty() && !state.loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(t("incidencias.sin_datos"), color = TextMuted, fontSize = 15.sp)
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaIncidenciaDialog(
    creando:      Boolean,
    error:        String?,
    mensajeExito: String?,
    onDismiss:    () -> Unit,
    onEnviar:     (tipo: String, descripcion: String) -> Unit
) {
    val lang by LanguageManager.lang.collectAsState()
    fun t(key: String) = LanguageManager.t(key)

    val tipos = listOf("GPS", "retraso", "olvido", "otro")
    var tipoSeleccionado by remember { mutableStateOf(tipos[0]) }
    var descripcion      by remember { mutableStateOf("") }
    var expandido        by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title  = { Text(t("incidencias.nueva_titulo"), fontWeight = FontWeight.Bold) },
        text   = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                ExposedDropdownMenuBox(expanded = expandido, onExpandedChange = { expandido = !expandido }) {
                    OutlinedTextField(
                        value         = tipoSeleccionado,
                        onValueChange = {},
                        readOnly      = true,
                        label         = { Text(t("incidencias.tipo")) },
                        trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
                        modifier      = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
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
                    label         = { Text(t("incidencias.descripcion")) },
                    minLines      = 2,
                    modifier      = Modifier.fillMaxWidth()
                )

                if (!error.isNullOrBlank()) {
                    Text(error, color = Red, fontSize = 12.sp)
                }
                if (!mensajeExito.isNullOrBlank()) {
                    Text(mensajeExito, color = Green, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick  = { onEnviar(tipoSeleccionado, descripcion) },
                enabled  = !creando,
                colors   = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                if (creando) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text(t("incidencias.enviar"))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(t("incidencias.cancelar")) }
        }
    )
}

@Composable
fun IncidenciaItem(i: IncidenciaResponse) {
    fun t(key: String) = LanguageManager.t(key)

    val (badgeColor, textColor) = when (i.tipo.lowercase()) {
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
                Surface(shape = RoundedCornerShape(20.dp), color = badgeColor) {
                    Text(
                        i.tipo.replaceFirstChar { it.uppercase() },
                        color      = textColor,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier   = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (i.resuelta) GreenBg else RedBg
                ) {
                    Text(
                        if (i.resuelta) t("incidencias.resuelta") else t("incidencias.abierta"),
                        color      = if (i.resuelta) Green else Red,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier   = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }
            if (!i.descripcion.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(i.descripcion, color = TextMuted, fontSize = 13.sp)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                i.fecha?.take(16)?.replace("T", " ") ?: "—",
                color    = TextLabel,
                fontSize = 11.sp
            )
        }
    }
}