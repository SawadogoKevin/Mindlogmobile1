package com.mindforce.mindlog.data.model

data class PersonnelResponse(
    val id: Long,
    val nom: String,
    val prenom: String,
    val matricule: String?,
    val email: String?,
    val telephone: String?,
    val poste: String?,
    val actif: Boolean,
    val departementId: Long?,
    val departementNom: String?,
    val role: String?,
    val dateCreation: String?
)
