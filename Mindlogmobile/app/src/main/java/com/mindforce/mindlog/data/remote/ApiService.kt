package com.mindforce.mindlog.data.remote

import com.mindforce.mindlog.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ===== AUTH =====

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/verify")
    suspend fun verify(@Body request: VerificationRequest): Response<LoginResponse>

    @POST("api/auth/logout")
    suspend fun logout(): Response<String>

    // ===== DASHBOARD =====

    @GET("api/dashboard/chef/{userId}")
    suspend fun getDashboardStats(@Path("userId") userId: Long): Response<DashboardStats>

    // ===== MATERIELS (Chef de Département) =====

    /** Matériels affectés au département du chef connecté */
    @GET("api/affectations/departement/{departementId}/actives")
    suspend fun getMesMateriels(@Path("departementId") departementId: Long): Response<List<AffectationMaterielResponse>>

    @GET("api/materiels/{id}")
    suspend fun getMateriel(@Path("id") id: String): Response<MaterielResponse>

    // ===== PERSONNELS =====

    @GET("api/personnels/departement/{departementId}")
    suspend fun getPersonnelsParDepartement(@Path("departementId") departementId: Long): Response<List<PersonnelResponse>>

    // ===== PANNES (Chef de Département) =====

    /**
     * Signale une panne — multipart/form-data
     */
    @Multipart
    @POST("api/pannes")
    suspend fun signalerPanne(
        @Part("materielId") materielId: RequestBody,
        @Part("typePanne") typePanne: RequestBody,
        @Part("descriptionPanne") descriptionPanne: RequestBody,
        @Part("signaleParId") signaleParId: RequestBody,
        @Part justificatifs: List<MultipartBody.Part>
    ): Response<PanneResponse>

    @GET("api/pannes")
    suspend fun getMesSignalements(): Response<List<PanneResponse>>

    @GET("api/pannes/materiel/{materielId}")
    suspend fun getHistoriquePannes(@Path("materielId") materielId: String): Response<List<PanneResponse>>
}
