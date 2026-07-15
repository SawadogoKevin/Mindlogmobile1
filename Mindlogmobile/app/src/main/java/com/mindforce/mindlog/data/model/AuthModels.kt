package com.mindforce.mindlog.data.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class VerificationRequest(
    val email: String,
    val code: String
)

data class LoginResponse(
    val token: String?,
    val id: Long?,
    val email: String?,
    val role: String?,
    val nom: String?,
    val prenom: String?,
    val actif: Boolean?,
    val departementId: Long?,
    val departementNom: String?,
    val needVerification: Boolean,
    val message: String?
)
