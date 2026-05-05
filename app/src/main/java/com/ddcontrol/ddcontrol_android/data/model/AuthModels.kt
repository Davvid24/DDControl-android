package com.ddcontrol.ddcontrol_android.data.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val idUsuario: Int,
    val idEmpresa: Int,
    val nombre: String,
    val apellidos: String,
    val rol: String
)