package com.mindforce.mindlog.data.repository

import com.mindforce.mindlog.data.local.SessionManager
import com.mindforce.mindlog.data.model.LoginRequest
import com.mindforce.mindlog.data.model.VerificationRequest
import com.mindforce.mindlog.data.remote.ApiService
import com.mindforce.mindlog.data.remote.NetworkUtils

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
}

class AuthRepository(
    private val api: ApiService,
    private val sessionManager: SessionManager
) {
    /** Étape 1 : email + mot de passe -> envoi du code par email */
    suspend fun login(email: String, password: String): ApiResult<String> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()
                body?.departementId?.let { 
                    sessionManager.saveDepartementId(it) 
                }
                ApiResult.Success(body?.message ?: "Code envoyé par email")
            } else {
                ApiResult.Error(NetworkUtils.extractErrorMessage(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Impossible de contacter le serveur")
        }
    }

    /** Étape 2 : vérification du code 2FA -> token JWT + session */
    suspend fun verify(email: String, code: String): ApiResult<Unit> {
        return try {
            val response = api.verify(VerificationRequest(email, code))
            if (response.isSuccessful && response.body()?.token != null) {
                val body = response.body()!!
                sessionManager.saveSession(
                    token = body.token!!,
                    email = body.email ?: email,
                    role = body.role ?: "",
                    nom = body.nom,
                    prenom = body.prenom,
                    userId = body.id,
                    departementId = body.departementId,
                    departementNom = body.departementNom
                )
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(NetworkUtils.extractErrorMessage(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Impossible de contacter le serveur")
        }
    }

    suspend fun logout() {
        try {
            api.logout()
        } catch (_: Exception) {
            // Le token est stateless côté serveur : même en cas d'échec réseau,
            // on nettoie la session locale.
        }
        sessionManager.clear()
    }
}
