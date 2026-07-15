package com.mindforce.mindlog.data.repository

import com.mindforce.mindlog.data.model.DashboardStats
import com.mindforce.mindlog.data.remote.ApiService
import com.mindforce.mindlog.data.remote.NetworkUtils

class DashboardRepository(private val api: ApiService) {

    suspend fun getStats(userId: Long): ApiResult<DashboardStats> {
        return try {
            val response = api.getDashboardStats(userId)
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
