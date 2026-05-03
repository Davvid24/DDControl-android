package com.ddcontrol.ddcontrol_android.ui.solicitudes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ddcontrol.ddcontrol_android.data.model.SolicitudRequest
import com.ddcontrol.ddcontrol_android.data.model.SolicitudResponse
import com.ddcontrol.ddcontrol_android.data.repository.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SolicitudesState(
    val solicitudes: List<SolicitudResponse> = emptyList(),
    val loading:     Boolean = false,
    val error:       String? = null
)

class SolicitudesViewModel : ViewModel() {

    private val repo = SolicitudRepository()

    private val _state = MutableStateFlow(SolicitudesState())
    val state: StateFlow<SolicitudesState> = _state

    fun cargar(userId: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            when (val r = repo.getSolicitudes(userId)) {
                is Result.Success -> _state.value = SolicitudesState(
                    solicitudes = r.data.sortedByDescending { it.fechaSolicitud }
                )
                is Result.Error -> _state.value = SolicitudesState(error = r.message)
            }
        }
    }

    fun crear(userId: Int, tipo: String, inicio: String, fin: String, motivo: String) {
        viewModelScope.launch {
            val req = SolicitudRequest(
                idUsuario   = userId,
                tipo        = tipo,
                fechaInicio = inicio,
                fechaFin    = fin,
                motivo      = motivo.ifBlank { null }
            )
            when (val r = repo.createSolicitud(req)) {
                is Result.Success -> cargar(userId)
                is Result.Error   -> _state.value = _state.value.copy(error = r.message)
            }
        }
    }
}