package com.ddcontrol.ddcontrol_android.data.model

data class IncidenciaResponse(
    val id: Int,
    val idUsuario: Int,
    val nombreUsuario: String?,
    val tipo: String,
    val descripcion: String?,
    val fecha: String?,
    val resuelta: Boolean
)

data class IncidenciaRequest(
    val idUsuario: Int,
    val tipo: String,
    val descripcion: String?,
    val idFichaje: Int? = null
)