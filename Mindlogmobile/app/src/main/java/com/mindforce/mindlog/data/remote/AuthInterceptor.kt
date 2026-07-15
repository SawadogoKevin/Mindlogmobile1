package com.mindforce.mindlog.data.remote

import android.util.Log
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import com.mindforce.mindlog.data.local.SessionManager

class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        Log.d("AuthInterceptor", "Intercepting: ${original.method} ${original.url}")

        // Les endpoints /api/auth/** n'ont pas besoin de token
        if (original.url.encodedPath.startsWith("/api/auth")) {
            Log.d("AuthInterceptor", "Pas de token nécessaire pour auth")
            return chain.proceed(original)
        }

        val token = runBlocking { sessionManager.getToken() }
        Log.d("AuthInterceptor", "Token récupéré: ${token?.take(10)}...")

        val request = if (!token.isNullOrBlank()) {
            original.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            Log.w("AuthInterceptor", "ATTENTION: Aucun token trouvé dans la session")
            original
        }
        return chain.proceed(request)
    }
}
