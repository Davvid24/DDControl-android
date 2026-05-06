package com.ddcontrol.ddcontrol_android.data.model

data class CalendarioResponse(
    val turno: TurnoInfo?,
    val dias: List<DiaCalendario>
)

data class TurnoInfo(
    val nombre: String,
    val horaEntrada: String,
    val horaSalida: String,
    val diasSemana: List<String>
)

data class DiaCalendario(
    val fecha: String,
    val esDiaTurno: Boolean,
    val tieneFichaje: Boolean,
    val horaEntrada: String?,
    val horaSalida: String?,
    val dentroDeRadio: Boolean
)