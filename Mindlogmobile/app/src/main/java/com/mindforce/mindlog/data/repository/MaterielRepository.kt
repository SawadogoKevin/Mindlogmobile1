package com.mindforce.mindlog.data.repository

import com.mindforce.mindlog.data.model.AffectationMaterielResponse
import com.mindforce.mindlog.data.model.MaterielResponse
import com.mindforce.mindlog.data.remote.ApiService
import com.mindforce.mindlog.data.remote.NetworkUtils

class MaterielRepository(private val api: ApiService) {

    suspend fun getMesMateriels(departementId: Long): ApiResult<List<AffectationMaterielResponse>> {
        return try {
            val response = api.getMesMateriels(departementId)
            if (response.isSuccessful) {
                ApiResult.Success(response.body() ?: emptyList())
            } else {
                ApiResult.Error(NetworkUtils.extractErrorMessage(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Impossible de contacter le serveur")
        }
    }

    suspend fun getMateriel(id: String): ApiResult<MaterielResponse> {
        return try {
            val response = api.getMateriel(id)
            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(NetworkUtils.extractErrorMessage(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Impossible de contacter le serveur")
        }
    }
}
