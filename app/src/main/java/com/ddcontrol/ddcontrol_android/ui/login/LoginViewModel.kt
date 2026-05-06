package com.ddcontrol.ddcontrol_android.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ddcontrol.ddcontrol_android.data.api.RetrofitClient
import com.ddcontrol.ddcontrol_android.data.repository.AuthRepository
import com.ddcontrol.ddcontrol_android.data.repository.Result
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LoginState(
    val loading:   Boolean = false,
    val success:   Boolean = false,
    val error:     String? = null,
    val token:     String? = null,
    val userId:    Int?    = null,
    val empresaId: Int?    = null,
    val nombre:    String? = null,
    val rol:       String? = null
)

class LoginViewModel : ViewModel() {

    private val repo = AuthRepository()

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = _state.value.copy(error = "Email y contraseña son obligatorios")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            when (val result = repo.login(email, password)) {
                is Result.Success -> {
                    RetrofitClient.setToken(result.data.token)

                    FirebaseMessaging.getInstance().token.addOnSuccessListener { fcmToken ->
                        viewModelScope.launch(Dispatchers.IO) {
                            try {
                                RetrofitClient.instance.actualizarFcmToken(
                                    result.data.idUsuario,
                                    mapOf("fcmToken" to fcmToken)
                                )
                            } catch (_: Exception) {}
                        }
                    }

                    _state.value = LoginState(
                        success   = true,
                        token     = result.data.token,
                        userId    = result.data.idUsuario,
                        empresaId = result.data.idEmpresa,
                        nombre    = "${result.data.nombre} ${result.data.apellidos}",
                        rol       = result.data.rol
                    )
                }
                is Result.Error -> {
                    _state.value = LoginState(error = result.message)
                }
            }
        }
    }
}