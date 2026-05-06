package com.ddcontrol.ddcontrol_android.ui.fichajes

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ddcontrol.ddcontrol_android.data.model.FichajeResponse
import com.ddcontrol.ddcontrol_android.data.repository.*
import com.ddcontrol.ddcontrol_android.data.api.RetrofitClient
import com.ddcontrol.ddcontrol_android.data.model.FichajeRequest
import com.ddcontrol.ddcontrol_android.util.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class FichajesState(
    val fichajesHoy: List<FichajeResponse> = emptyList(),
    val loadingLista: Boolean = false,
    val loading: Boolean = false,
    val loadingPausa: Boolean = false,
    val error: String? = null,
    val aviso: String? = null,
    val mensaje: String? = null,
    val siguienteTipo: String = "entrada",
    val enPausa: Boolean = false,
    val puedePonerPausa: Boolean = false,
    val turnoHoraEntrada: String? = null,
    val turnoHoraSalida: String? = null,
    val turnoNombre: String? = null,
    val esDiaTurno: Boolean = false,
    val idSedeEmpleado: Int? = null,
    val nombreSedeEmpleado: String? = null
)
class FichajesViewModel(private val appContext: Context) : ViewModel() {

    private val fichajeRepo = FichajeRepository()
    private val api = RetrofitClient.instance

    private val _state = MutableStateFlow(FichajesState())
    val state: StateFlow<FichajesState> = _state

    fun cargar(userId: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loadingLista = true, error = null)

            val hoy = LocalDate.now()

            try {
                val calResp = api.getCalendario(userId, hoy.year, hoy.monthValue)
                if (calResp.isSuccessful && calResp.body() != null) {
                    val body = calResp.body()!!
                    val diaHoy = body.dias.find { it.fecha == hoy.toString() }
                    _state.value = _state.value.copy(
                        turnoNombre      = body.turno?.nombre,
                        turnoHoraEntrada = body.turno?.horaEntrada,
                        turnoHoraSalida  = body.turno?.horaSalida,
                        esDiaTurno       = diaHoy?.esDiaTurno ?: false
                    )
                }
            } catch (_: Exception) {}

            when (val sedeResp = fichajeRepo.getSedeEmpleado(userId)) {
                is Result.Success -> {
                    val sede = sedeResp.data.firstOrNull()
                    _state.value = _state.value.copy(
                        idSedeEmpleado    = sede?.idSede,
                        nombreSedeEmpleado = sede?.nombreSede
                    )
                }
                else -> {}
            }

            when (val r = fichajeRepo.getFichajes(userId)) {
                is Result.Success -> {
                    val hoyStr = hoy.toString()
                    val hoyList = r.data
                        .filter { it.timestampFicha?.startsWith(hoyStr) == true }
                        .sortedByDescending { it.timestampFicha }
                    val ultimo = r.data.maxByOrNull { it.timestampFicha ?: "" }

                    val siguienteTipo = when (ultimo?.tipo) {
                        "entrada", "pausa_fin" -> "salida"
                        else -> "entrada"
                    }
                    val enPausa = ultimo?.tipo == "pausa_inicio"
                    val puedePonerPausa = ultimo?.tipo == "entrada" || ultimo?.tipo == "pausa_fin"

                    _state.value = _state.value.copy(
                        fichajesHoy      = hoyList,
                        loadingLista     = false,
                        siguienteTipo    = siguienteTipo,
                        enPausa          = enPausa,
                        puedePonerPausa  = puedePonerPausa
                    )
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(loadingLista = false, error = r.message)
                }
            }
        }
    }
    fun ficharPausa(userId: Int, lat: Double, lon: Double) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loadingPausa = true, error = null, mensaje = null)

            val idSede = _state.value.idSedeEmpleado
            if (idSede == null) {
                _state.value = _state.value.copy(loadingPausa = false, error = "No tienes sede asignada")
                return@launch
            }

            val tipoPausa = if (_state.value.enPausa) "pausa_fin" else "pausa_inicio"

            val req = FichajeRequest(
                idUsuario = userId,
                idSede = idSede,
                tipo = tipoPausa,
                latitudReal = lat,
                longitudReal = lon
            )

            when (val r = fichajeRepo.fichar(req)) {
                is Result.Success -> {
                    val mensajePausa = if (tipoPausa == "pausa_inicio") "Pausa iniciada" else "Pausa finalizada"
                    _state.value = _state.value.copy(
                        loadingPausa = false,
                        mensaje = mensajePausa
                    )
                    cargar(userId)
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(loadingPausa = false, error = r.message)
                }
            }
        }
    }

    fun fichar(userId: Int, empresaId: Int, lat: Double, lon: Double) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null, mensaje = null, aviso = null)

            val horaEntrada = _state.value.turnoHoraEntrada
            val horaSalida = _state.value.turnoHoraSalida
            val esDiaTurno = _state.value.esDiaTurno

            if (!esDiaTurno) {
                _state.value = _state.value.copy(
                    loading = false,
                    aviso = "Hoy no es tu día de trabajo según tu turno. ¿Seguro que quieres fichar?"
                )
                return@launch
            }

            if (horaEntrada != null && horaSalida != null) {
                val ahora = java.time.LocalTime.now()
                val entrada = java.time.LocalTime.parse(horaEntrada)
                val salida = java.time.LocalTime.parse(horaSalida)
                val margen = java.time.Duration.ofMinutes(30)
                if (ahora.isBefore(entrada.minus(margen)) || ahora.isAfter(salida.plus(margen))) {
                    _state.value = _state.value.copy(
                        loading = false,
                        aviso = "Estás fichando fuera de tu horario (${horaEntrada.take(5)}–${horaSalida.take(5)}). Se registrará igualmente."
                    )
                    return@launch
                }
            }

            ejecutarFichaje(userId, lat, lon)
        }
    }

    fun confirmarFichajeConAviso(userId: Int, empresaId: Int, lat: Double, lon: Double) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, aviso = null, error = null, mensaje = null)
            ejecutarFichaje(userId, lat, lon)
        }
    }

    private suspend fun ejecutarFichaje(userId: Int, lat: Double, lon: Double) {
        val idSede = _state.value.idSedeEmpleado
        if (idSede == null) {
            _state.value = _state.value.copy(loading = false, error = "No tienes sede asignada")
            return
        }

        val tipo = _state.value.siguienteTipo
        val req = FichajeRequest(
            idUsuario = userId,
            idSede = idSede,
            tipo = tipo,
            latitudReal = lat,
            longitudReal = lon
        )

        when (val r = fichajeRepo.fichar(req)) {
            is Result.Success -> {
                val dentroDeRadio = r.data.dentroDeRadio ?: false
                if (!dentroDeRadio) {
                    NotificationHelper.show(
                        context = appContext,
                        id = 4001,
                        channelId = NotificationHelper.CHANNEL_FICHAJE,
                        title = "Fichaje fuera de radio",
                        message = "Has fichado fuera del radio permitido de tu sede."
                    )
                }
                _state.value = _state.value.copy(
                    loading = false,
                    mensaje = "${tipo.replaceFirstChar { it.uppercase() }} registrada correctamente"
                )
                cargar(userId)
            }
            is Result.Error -> {
                _state.value = _state.value.copy(loading = false, error = r.message)
            }
        }
    }

    fun descartarAviso() {
        _state.value = _state.value.copy(aviso = null)
    }
}