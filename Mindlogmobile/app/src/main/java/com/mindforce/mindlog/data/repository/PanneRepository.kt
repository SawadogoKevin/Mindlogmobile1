package com.mindforce.mindlog.data.repository

import com.google.gson.Gson
import com.mindforce.mindlog.data.model.PanneRequest
import com.mindforce.mindlog.data.model.PanneResponse
import com.mindforce.mindlog.data.remote.ApiService
import com.mindforce.mindlog.data.remote.NetworkUtils
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class PanneRepository(private val api: ApiService) {

    /**
     * Signale une panne avec ses photos justificatives (0 à plusieurs).
     */
    suspend fun signaler(
        materielId: String,
        typePanne: String,
        description: String,
        userId: Long,
        photoFiles: List<File>
    ): ApiResult<PanneResponse> {
        return try {
            val materielIdBody = materielId.toRequestBody("text/plain".toMediaTypeOrNull())
            val typePanneBody = typePanne.toRequestBody("text/plain".toMediaTypeOrNull())
            val descriptionBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
            val signaleParIdBody = userId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            val photoParts = photoFiles.map { file ->
                val mimeType = when (file.extension.lowercase()) {
                    "png" -> "image/png"
                    "webp" -> "image/webp"
                    "heic" -> "image/heic"
                    else -> "image/jpeg"
                }
                val body = file.asRequestBody(mimeType.toMediaTypeOrNull())
                MultipartBody.Part.createFormData("justificatifs", file.name, body)
            }

            val response = api.signalerPanne(
                materielIdBody,
                typePanneBody,
                descriptionBody,
                signaleParIdBody,
                photoParts
            )

            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(NetworkUtils.extractErrorMessage(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Impossible de contacter le serveur")
        }
    }

    suspend fun getMesSignalements(): ApiResult<List<PanneResponse>> {
        return try {
            val response = api.getMesSignalements()
            if (response.isSuccessful) {
                ApiResult.Success(response.body() ?: emptyList())
            } else {
                ApiResult.Error(NetworkUtils.extractErrorMessage(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Impossible de contacter le serveur")
        }
    }

    suspend fun getHistorique(materielId: String): ApiResult<List<PanneResponse>> {
        return try {
            val response = api.getHistoriquePannes(materielId)
            if (response.isSuccessful) {
                ApiResult.Success(response.body() ?: emptyList())
            } else {
                ApiResult.Error(NetworkUtils.extractErrorMessage(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Impossible de contacter le serveur")
        }
    }
}
