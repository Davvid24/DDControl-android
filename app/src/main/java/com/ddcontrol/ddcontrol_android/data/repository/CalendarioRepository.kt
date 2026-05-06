package com.ddcontrol.ddcontrol_android.data.repository

import com.ddcontrol.ddcontrol_android.data.api.RetrofitClient
import com.ddcontrol.ddcontrol_android.data.model.CalendarioResponse

class CalendarioRepository {
    private val api = RetrofitClient.instance

    suspend fun getCalendario(userId: Int, year: Int, month: Int): Result<CalendarioResponse> {
        return try {
            val r = api.getCalendario(userId, year, month)
            if (r.isSuccessful && r.body() != null) Result.Success(r.body()!!)
            else Result.Error("Error ${r.code()}")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error desconocido")
        }
    }
}