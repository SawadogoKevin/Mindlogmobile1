package com.mindforce.mindlog.data.repository

import com.mindforce.mindlog.data.model.PersonnelResponse
import com.mindforce.mindlog.data.remote.ApiService
import com.mindforce.mindlog.data.remote.NetworkUtils

class PersonnelRepository(private val api: ApiService) {

    suspend fun getPersonnels(departementId: Long): ApiResult<List<PersonnelResponse>> {
        return try {
            val response = api.getPersonnelsParDepartement(departementId)
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
