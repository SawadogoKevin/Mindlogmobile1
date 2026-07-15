package com.mindforce.mindlog.data.remote

import com.mindforce.mindlog.data.local.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    /**
     * ⚠️ À ADAPTER selon votre environnement :
     * - Émulateur Android Studio -> l'hôte "localhost" de la machine se joint via 10.0.2.2
     * - Téléphone physique -> mettre l'adresse IP locale de la machine qui héberge le backend
     *   (ex: "http://192.168.1.50:8080/")
     * - Backend déployé -> mettre l'URL publique (https://...)
     */
    const val BASE_URL = "http://192.168.43.151:8080/" // Default for Emulator, change to your IP for physical device

    fun create(sessionManager: SessionManager): ApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionManager))
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
