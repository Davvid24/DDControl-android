package com.ddcontrol.ddcontrol_android.data.model

data class SedeResponse(
    val id: Int,
    val nombre: String,
    val direccion: String?,
    val latitud: Double,
    val longitud: Double,
    val radioMetros: Int,
    val activa: Boolean
)