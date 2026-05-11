package com.ddcontrol.ddcontrol_android.data.api

import com.ddcontrol.ddcontrol_android.util.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
object RetrofitClient {

    @Volatile private var token: String? = null
    @Volatile private var onUnauthorized: (() -> Unit)? = null

    fun setToken(t: String?) { token = t }
    fun setOnUnauthorized(callback: () -> Unit) { onUnauthorized = callback }

    val instance: ApiService by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val currentToken = token
                val request = if (currentToken != null) {
                    chain.request().newBuilder()
                        .header("Authorization", "Bearer $currentToken")
                        .build()
                } else {
                    chain.request()
                }
                val response = chain.proceed(request)
                if (response.code == 401 || response.code == 403) {
                    onUnauthorized?.invoke()
                }
                response
            }
            .build()

        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}