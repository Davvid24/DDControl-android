package com.ddcontrol.ddcontrol_android.data.repository

import com.ddcontrol.ddcontrol_android.data.api.RetrofitClient
import com.ddcontrol.ddcontrol_android.data.model.EmpleadoSedeResponse
import com.ddcontrol.ddcontrol_android.data.model.FichajeRequest
import com.ddcontrol.ddcontrol_android.data.model.FichajeResponse
import com.ddcontrol.ddcontrol_android.data.model.SedeResponse

class FichajeRepository {
    private val api = RetrofitClient.instance

    suspend fun getFichajes(idUsuario: Int): Result<List<FichajeResponse>> {
        return try {
            val r = api.getFichajesByUsuario(idUsuario)
            if (r.isSuccessful) Result.Success(r.body() ?: emptyList())
            else Result.Error("Error ${r.code()}")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error desconocido")
        }
    }

    suspend fun fichar(request: FichajeRequest): Result<FichajeResponse> {
        return try {
            val r = api.createFichaje(request)
            if (r.isSuccessful && r.body() != null) Result.Success(r.body()!!)
            else Result.Error("Error al fichar: ${r.code()}")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error desconocido")
        }
    }

    suspend fun getSedes(idEmpresa: Int): Result<List<SedeResponse>> {
        return try {
            val r = api.getSedesByEmpresa(idEmpresa)
            if (r.isSuccessful) Result.Success(r.body() ?: emptyList())
            else Result.Error("Error ${r.code()}")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error desconocido")
        }
    }
    suspend fun getSedeEmpleado(idUsuario: Int): Result<List<EmpleadoSedeResponse>> {
        return try {
            val r = api.getSedesByUsuario(idUsuario)
            if (r.isSuccessful) Result.Success(r.body() ?: emptyList())
            else Result.Error("Error ${r.code()}")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error desconocido")
        }
    }
}