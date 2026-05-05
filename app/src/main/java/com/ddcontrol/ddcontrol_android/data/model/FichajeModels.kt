package com.ddcontrol.ddcontrol_android.data.model

data class FichajeRequest(
    val idUsuario: Int,
    val idSede: Int,
    val tipo: String,
    val latitudReal: Double,
    val longitudReal: Double,
    val metodo: String = "movil",
    val observaciones: String? = null
)

data class FichajeResponse(
    val id: Int,
    val idUsuario: Int,
    val nombreUsuario: String?,
    val idSede: Int,
    val nombreSede: String?,
    val tipo: String,
    val timestampFicha: String?,
    val latitudReal: Double?,
    val longitudReal: Double?,
    val dentroDeRadio: Boolean?,
    val metodo: String?,
    val observaciones: String?
)