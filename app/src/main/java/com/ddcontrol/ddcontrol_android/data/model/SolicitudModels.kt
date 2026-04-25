package com.ddcontrol.ddcontrol_android.data.model

data class SolicitudRequest(
    val idUsuario: Int,
    val tipo: String,
    val fechaInicio: String,
    val fechaFin: String,
    val motivo: String?
)

data class SolicitudResponse(
    val id: Int,
    val idUsuario: Int,
    val nombreUsuario: String?,
    val tipo: String,
    val fechaInicio: String?,
    val fechaFin: String?,
    val motivo: String?,
    val estado: String,
    val fechaSolicitud: String?,
    val comentarioAdmin: String?
)