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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ddcontrol.ddcontrol_android.data.model.FichajeResponse
import com.ddcontrol.ddcontrol_android.ui.theme.*
import com.ddcontrol.ddcontrol_android.util.SessionManager
import com.google.android.gms.location.LocationServices

@SuppressLint("MissingPermission")
@Composable
fun FichajesScreen(
    session: SessionManager,
    vm: FichajesViewModel = viewModel()
) {
    val state   by vm.state.collectAsState()
    val context = LocalContext.current
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                vm.fichar(
                    userId    = session.getUserId(),
                    empresaId = session.getEmpresaId(),
                    lat       = loc?.latitude ?: 0.0,
                    lon       = loc?.longitude ?: 0.0
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        vm.cargarFichajes(session.getUserId())
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
            Column(Modifier.padding(20.dp)) {
                Text(
                    text  = state.estadoTexto,
                    color = Navy,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    enabled  = !state.loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape  = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.siguienteTipo == "entrada") Green else Red
                    )
                ) {
                    if (state.loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(
                            if (state.siguienteTipo == "entrada") "Registrar entrada" else "Registrar salida",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (state.error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(state.error!!, color = Red, fontSize = 13.sp)
                }
                if (state.mensaje != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(state.mensaje!!, color = Green, fontSize = 13.sp)
                }
            }
        }

        Text(
            "MIS FICHAJES",
            color = TextLabel,
            fontSize = 11.sp,
            modifier = Modifier.padding(16.dp)
        )

        if (state.loadingLista) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(state.fichajes) { fichaje ->
                    FichajeItem(fichaje)
                }
            }
        }
    }
}

@Composable
fun FichajeItem(f: FichajeResponse) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(10.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (f.tipo == "entrada") GreenBg else RedBg,
                modifier = Modifier.width(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        if (f.tipo == "entrada") "Entrada" else "Salida",
                        color = if (f.tipo == "entrada") Green else Red,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    f.timestampFicha?.take(16)?.replace("T", " ") ?: "—",
                    color = Navy,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(f.nombreSede ?: "—", color = TextMuted, fontSize = 12.sp)
            }
            Text(
                if (f.dentroDeRadio == true) "✓" else "✗",
                color = if (f.dentroDeRadio == true) Green else Red,
                fontSize = 16.sp
            )
        }
    }
}