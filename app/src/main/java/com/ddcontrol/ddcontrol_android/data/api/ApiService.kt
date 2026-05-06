package com.ddcontrol.ddcontrol_android.data.api

import com.ddcontrol.ddcontrol_android.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/usuarios/{id}")
    suspend fun getUsuario(@Path("id") id: Int): Response<UsuarioResponse>

    @GET("api/fichajes/usuario/{idUsuario}")
    suspend fun getFichajesByUsuario(@Path("idUsuario") idUsuario: Int): Response<List<FichajeResponse>>

    @POST("api/fichajes")
    suspend fun createFichaje(@Body request: FichajeRequest): Response<FichajeResponse>

    @GET("api/solicitudes/usuario/{idUsuario}")
    suspend fun getSolicitudesByUsuario(@Path("idUsuario") idUsuario: Int): Response<List<SolicitudResponse>>

    @POST("api/solicitudes")
    suspend fun createSolicitud(@Body request: SolicitudRequest): Response<SolicitudResponse>

    @GET("api/incidencias/usuario/{idUsuario}")
    suspend fun getIncidenciasByUsuario(@Path("idUsuario") idUsuario: Int): Response<List<IncidenciaResponse>>

    @POST("api/incidencias")
    suspend fun createIncidencia(@Body request: IncidenciaRequest): Response<IncidenciaResponse>

    @GET("api/empleado-sede/usuario/{idUsuario}")
    suspend fun getSedesByUsuario(@Path("idUsuario") idUsuario: Int): Response<List<EmpleadoSedeResponse>>
    @GET("api/sedes/empresa/{idEmpresa}")
    suspend fun getSedesByEmpresa(@Path("idEmpresa") idEmpresa: Int): Response<List<SedeResponse>>

    @GET("api/calendario/{idUsuario}")
    suspend fun getCalendario(
        @Path("idUsuario") idUsuario: Int,
        @Query("year") year: Int,
        @Query("month") month: Int
    ): Response<CalendarioResponse>
    @PATCH("api/usuarios/{id}/fcm-token")
    suspend fun actualizarFcmToken(
        @Path("id") id: Int,
        @Body body: Map<String, String>
    ): Response<Void>
}