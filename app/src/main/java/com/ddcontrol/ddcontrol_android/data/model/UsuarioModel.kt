package com.ddcontrol.ddcontrol_android.data.model

data class UsuarioResponse(
    val id: Int,
    val nombre: String,
    val apellidos: String,
    val email: String,
    val rol: String,
    val tipoEmpleado: String,
    val telefono: String?,
    val activo: Boolean,
    val idTurno: Int?,
    val nombreTurno: String?,
    val nombreEmpresa: String?
)