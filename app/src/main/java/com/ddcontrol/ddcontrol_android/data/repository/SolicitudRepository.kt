package com.ddcontrol.ddcontrol_android.data.repository

import com.ddcontrol.ddcontrol_android.data.api.RetrofitClient
import com.ddcontrol.ddcontrol_android.data.model.SolicitudRequest
import com.ddcontrol.ddcontrol_android.data.model.SolicitudResponse

class SolicitudRepository {
    private val api = RetrofitClient.instance

    suspend fun getSolicitudes(idUsuario: Int): Result<List<SolicitudResponse>> {
        return try {
            val r = api.getSolicitudesByUsuario(idUsuario)
            if (r.isSuccessful) Result.Success(r.body() ?: emptyList())
            else Result.Error("Error ${r.code()}")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error desconocido")
        }
    }

    suspend fun createSolicitud(request: SolicitudRequest): Result<SolicitudResponse> {
        return try {
            val r = api.createSolicitud(request)
            if (r.isSuccessful && r.body() != null) Result.Success(r.body()!!)
            else Result.Error("Error al crear solicitud: ${r.code()}")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error desconocido")
        }
    }
}