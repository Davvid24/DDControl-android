package com.ddcontrol.ddcontrol_android.ui.incidencias

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ddcontrol.ddcontrol_android.data.model.IncidenciaRequest
import com.ddcontrol.ddcontrol_android.data.model.IncidenciaResponse
import com.ddcontrol.ddcontrol_android.data.repository.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class IncidenciasState(
    val incidencias:    List<IncidenciaResponse> = emptyList(),
    val loading:        Boolean = false,
    val error:          String? = null,
    val creando:        Boolean = false,
    val mensajeExito:   String? = null
)

class IncidenciasViewModel : ViewModel() {

    private val repo = IncidenciaRepository()

    private val _state = MutableStateFlow(IncidenciasState())
    val state: StateFlow<IncidenciasState> = _state

    fun cargar(userId: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            when (val r = repo.getIncidencias(userId)) {
                is Result.Success -> _state.value = IncidenciasState(
                    incidencias = r.data.sortedByDescending { it.fecha }
                )
                is Result.Error -> _state.value = IncidenciasState(error = r.message)
            }
        }
    }

    fun crear(userId: Int, tipo: String, descripcion: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(creando = true, error = null, mensajeExito = null)
            val req = IncidenciaRequest(
                idUsuario = userId,
                tipo = tipo,
                descripcion = descripcion.ifBlank { null }
            )
            when (val r = repo.createIncidencia(req)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        creando       = false,
                        mensajeExito  = "Incidencia enviada correctamente"
                    )
                    cargar(userId)
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(creando = false, error = r.message)
                }
            }
        }
    }
}