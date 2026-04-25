package com.ddcontrol.ddcontrol_android.data.repository

import com.ddcontrol.ddcontrol_android.data.api.RetrofitClient
import com.ddcontrol.ddcontrol_android.data.model.IncidenciaResponse

class IncidenciaRepository {
    private val api = RetrofitClient.instance

    suspend fun getIncidencias(idUsuario: Int): Result<List<IncidenciaResponse>> {
        return try {
            val r = api.getIncidenciasByUsuario(idUsuario)
            if (r.isSuccessful) Result.Success(r.body() ?: emptyList())
            else Result.Error("Error ${r.code()}")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error desconocido")
        }
    }
}