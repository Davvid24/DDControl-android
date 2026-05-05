package com.ddcontrol.ddcontrol_android.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ddcontrol.ddcontrol_android.ui.theme.*
import com.ddcontrol.ddcontrol_android.util.SessionManager

@Composable
fun LoginScreen(
    session: SessionManager,
    onLoginSuccess: () -> Unit,
    vm: LoginViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    val focusManager = LocalFocusManager.current

    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(state.success) {
        if (state.success) {
            session.saveSession(
                token     = state.token!!,
                userId    = state.userId!!,
                empresaId = state.empresaId!!,
                nombre    = state.nombre!!,
                rol       = state.rol!!
            )
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Indigo,
                modifier = Modifier.size(60.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("DD", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            Text("D&D Control", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text("Panel de empleado", color = TextMuted, fontSize = 14.sp)

            Spacer(Modifier.height(40.dp))

            OutlinedTextField(
                value         = email,
                onValueChange = { email = it },
                label         = { Text("Correo electrónico") },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction    = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor   = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor  = Primary,
                    unfocusedLabelColor = TextLabel,
                    focusedBorderColor  = Primary,
                    unfocusedBorderColor = Border
                )
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value         = password,
                onValueChange = { password = it },
                label         = { Text("Contraseña") },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction    = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        vm.login(email, password)
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor   = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor  = Primary,
                    unfocusedLabelColor = TextLabel,
                    focusedBorderColor  = Primary,
                    unfocusedBorderColor = Border
                )
            )

            Spacer(Modifier.height(12.dp))

            // Error
            if (state.error != null) {
                Text(
                    text     = state.error!!,
                    color    = Red,
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
            }

            // Botón
            Button(
                onClick  = { vm.login(email, password) },
                enabled  = !state.loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape  = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Indigo)
            ) {
                if (state.loading) {
                    CircularProgressIndicator(
                        color    = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Iniciar sesión", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}