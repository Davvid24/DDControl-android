package com.ddcontrol.ddcontrol_android.ui.fichajes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ddcontrol.ddcontrol_android.data.model.FichajeRequest
import com.ddcontrol.ddcontrol_android.data.model.FichajeResponse
import com.ddcontrol.ddcontrol_android.data.repository.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class FichajesState(
    val fichajes:      List<FichajeResponse> = emptyList(),
    val loadingLista:  Boolean = false,
    val loading:       Boolean = false,
    val error:         String? = null,
    val mensaje:       String? = null,
    val siguienteTipo: String  = "entrada",
    val estadoTexto:   String  = "Sin registros hoy"
)

class FichajesViewModel : ViewModel() {

    private val fichajeRepo = FichajeRepository()

    private val _state = MutableStateFlow(FichajesState())
    val state: StateFlow<FichajesState> = _state

    fun cargarFichajes(userId: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loadingLista = true)
            when (val r = fichajeRepo.getFichajes(userId)) {
                is Result.Success -> {
                    val ultimo = r.data.maxByOrNull { it.timestampFicha ?: "" }
                    val siguiente = if (ultimo?.tipo == "entrada") "salida" else "entrada"
                    val estadoTexto = when (ultimo?.tipo) {
                        "entrada" -> "Estado: dentro"
                        "salida"  -> "Estado: fuera"
                        else      -> "Sin registros hoy"
                    }
                    _state.value = _state.value.copy(
                        fichajes      = r.data.sortedByDescending { it.timestampFicha },
                        loadingLista  = false,
                        siguienteTipo = siguiente,
                        estadoTexto   = estadoTexto
                    )
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(loadingLista = false, error = r.message)
                }
            }
        }
    }

    fun fichar(userId: Int, empresaId: Int, lat: Double, lon: Double) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null, mensaje = null)

            // Obtener primera sede de la empresa
            when (val sedes = fichajeRepo.getSedes(empresaId)) {
                is Result.Success -> {
                    val sede = sedes.data.firstOrNull()
                    if (sede == null) {
                        _state.value = _state.value.copy(loading = false, error = "No hay sedes disponibles")
                        return@launch
                    }
                    val tipo = _state.value.siguienteTipo
                    val req  = FichajeRequest(
                        idUsuario    = userId,
                        idSede       = sede.id,
                        tipo         = tipo,
                        latitudReal  = lat,
                        longitudReal = lon
                    )
                    when (val r = fichajeRepo.fichar(req)) {
                        is Result.Success -> {
                            _state.value = _state.value.copy(
                                loading = false,
                                mensaje = "${tipo.replaceFirstChar { it.uppercase() }} registrada correctamente"
                            )
                            cargarFichajes(userId)
                        }
                        is Result.Error -> {
                            _state.value = _state.value.copy(loading = false, error = r.message)
                        }
                    }
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(loading = false, error = sedes.message)
                }
            }
        }
    }
}