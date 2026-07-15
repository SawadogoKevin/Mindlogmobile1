package com.mindforce.mindlog.data.model

enum class TypePanne {
    REPARABLE, NON_REPARABLE
}

enum class StatutPanne {
    SIGNALE, EN_REPARATION, RESOLUE, DECLASSE
}

/** Correspond au "data" JSON envoyé en multipart lors du signalement */
data class PanneRequest(
    val descriptionPanne: String,
    val typePanne: TypePanne,
    val materielId: String,
    val signaleParId: Long
)

data class PanneResponse(
    val id: Long,
    val dateSignalement: String?,
    val descriptionPanne: String,
    val typePanne: TypePanne,
    val justificatifs: List<String>?,
    val dateResolution: String?,
    val statutEtape: StatutPanne,
    val materielId: String?,
    val materielMarque: String?,
    val materielModele: String?,
    val signaleParId: Long?,
    val signaleParNom: String?,
    val signaleParPrenom: String?,
    val traiteParId: Long?,
    val traiteParNom: String?,
    val traiteParPrenom: String?,
    val dateCreation: String?
)
