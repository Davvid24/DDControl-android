package com.ddcontrol.ddcontrol_android.ui.login

import SessionManager
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ddcontrol.ddcontrol_android.ui.theme.*
import com.ddcontrol.ddcontrol_android.util.LanguageManager

@Composable
fun LoginScreen(
    session: SessionManager,
    onLoginSuccess: () -> Unit,
    vm: LoginViewModel = viewModel()
) {
    val state       by vm.state.collectAsState()
    val lang        by LanguageManager.lang.collectAsState()
    val context     = LocalContext.current
    val focusManager = LocalFocusManager.current
    fun t(key: String) = LanguageManager.t(key)

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
        modifier         = Modifier.fillMaxSize().background(Navy),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier            = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                listOf("es", "en").forEach { code ->
                    val isActive = lang == code
                    Surface(
                        onClick  = { LanguageManager.setLang(context, code) },
                        shape    = RoundedCornerShape(6.dp),
                        color    = if (isActive) Primary else Color.White.copy(alpha = 0.08f),
                        modifier = Modifier.size(width = 36.dp, height = 28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                code.uppercase(),
                                fontSize   = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color      = if (isActive) Color.White else Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }
                    if (code == "es") Spacer(Modifier.width(4.dp))
                }
            }

            Spacer(Modifier.height(16.dp))

            Surface(shape = RoundedCornerShape(12.dp), color = Indigo, modifier = Modifier.size(60.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text("DD", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(t("login.titulo"),    color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text(t("login.subtitulo"), color = TextMuted,   fontSize = 14.sp)

            Spacer(Modifier.height(40.dp))

            OutlinedTextField(
                value         = email,
                onValueChange = { email = it },
                label         = { Text(t("login.email")) },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor    = Color.White,
                    unfocusedTextColor  = Color.White,
                    focusedLabelColor   = Primary,
                    unfocusedLabelColor = TextLabel,
                    focusedBorderColor  = Primary,
                    unfocusedBorderColor = Border
                )
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value         = password,
                onValueChange = { password = it },
                label         = { Text(t("login.password")) },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    vm.login(email.trim().replace("\n", ""), password.trim())
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor    = Color.White,
                    unfocusedTextColor  = Color.White,
                    focusedLabelColor   = Primary,
                    unfocusedLabelColor = TextLabel,
                    focusedBorderColor  = Primary,
                    unfocusedBorderColor = Border
                )
            )

            Spacer(Modifier.height(12.dp))

            if (state.error != null) {
                Text(text = state.error!!, color = Red, fontSize = 13.sp, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick  = { vm.login(email, password) },
                enabled  = !state.loading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(10.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Indigo)
            ) {
                if (state.loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(t("login.btn"), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}