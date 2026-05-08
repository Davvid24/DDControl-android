package com.ddcontrol.ddcontrol_android.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ddcontrol.ddcontrol_android.data.model.DiaCalendario
import com.ddcontrol.ddcontrol_android.data.repository.*
import com.ddcontrol.ddcontrol_android.util.LanguageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class DashboardState(
    val loading:               Boolean = true,
    val loadingCalendario:     Boolean = false,
    val nombreTurno:           String? = null,
    val turnoHoraEntrada:      String? = null,
    val turnoHoraSalida:       String? = null,
    val estadoFichaje:         String  = "",
    val ultimoFichaje:         String? = null,
    val fichajesHoy:           Int     = 0,
    val solicitudesPendientes: Int     = 0,
    val incidenciasAbiertas:   Int     = 0,
    val diasCalendario:        List<DiaCalendario> = emptyList()
)

class DashboardViewModel : ViewModel() {

    private val fichajeRepo    = FichajeRepository()
    private val solicitudRepo  = SolicitudRepository()
    private val incidenciaRepo = IncidenciaRepository()
    private val calendarioRepo = CalendarioRepository()

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state

    fun load(userId: Int, empresaId: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)

            when (val r = fichajeRepo.getFichajes(userId)) {
                is Result.Success -> {
                    val hoy     = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val hoyList = r.data.filter { it.timestampFicha?.startsWith(hoy) == true }
                    val ultimo  = r.data.maxByOrNull { it.timestampFicha ?: "" }
                    val estado  = when (ultimo?.tipo) {
                        "entrada"      -> LanguageManager.t("dashboard.estado_dentro")
                        "salida"       -> LanguageManager.t("dashboard.estado_fuera")
                        "pausa_inicio" -> LanguageManager.t("dashboard.estado_pausa")
                        "pausa_fin"    -> LanguageManager.t("dashboard.estado_pausa_fin")
                        else           -> LanguageManager.t("dashboard.estado_sin_reg")
                    }
                    _state.value = _state.value.copy(
                        fichajesHoy   = hoyList.size,
                        estadoFichaje = estado,
                        ultimoFichaje = ultimo?.timestampFicha?.take(16)?.replace("T", " ")
                    )
                }
                else -> {
                    _state.value = _state.value.copy(
                        estadoFichaje = LanguageManager.t("dashboard.estado_sin_reg")
                    )
                }
            }

            when (val r = solicitudRepo.getSolicitudes(userId)) {
                is Result.Success -> {
                    val pendientes = r.data.count { it.estado.lowercase() == "pendiente" }
                    _state.value = _state.value.copy(solicitudesPendientes = pendientes)
                }
                else -> {}
            }

            when (val r = incidenciaRepo.getIncidencias(userId)) {
                is Result.Success -> {
                    val abiertas = r.data.count { !it.resuelta }
                    _state.value = _state.value.copy(incidenciasAbiertas = abiertas)
                }
                else -> {}
            }

            _state.value = _state.value.copy(loading = false)
        }
    }

    fun loadCalendario(userId: Int, year: Int, month: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loadingCalendario = true)
            when (val r = calendarioRepo.getCalendario(userId, year, month)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        loadingCalendario = false,
                        diasCalendario    = r.data.dias,
                        nombreTurno       = r.data.turno?.nombre,
                        turnoHoraEntrada  = r.data.turno?.horaEntrada,
                        turnoHoraSalida   = r.data.turno?.horaSalida
                    )
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(loadingCalendario = false)
                }
            }
        }
    }
}