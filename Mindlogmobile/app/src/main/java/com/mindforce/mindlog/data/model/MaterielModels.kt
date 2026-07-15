package com.mindforce.mindlog.data.model

import com.google.gson.annotations.SerializedName

enum class EtatMateriel {
    @SerializedName("BON") BON,
    @SerializedName("USAGE") USAGE,
    @SerializedName("EN_PANNE") EN_PANNE,
    @SerializedName("MAINTENANCE") MAINTENANCE,
    @SerializedName("DECLASSE") DECLASSE,
    @SerializedName("HORS_SERVICE") HORS_SERVICE
}

data class MaterielResponse(
    val id: String,
    val marque: String,
    val modele: String,
    val numeroSerie: String?,
    val dateAcquisition: String?,
    val dateDebutUtilisation: String?,
    val fournisseur: String?,
    @SerializedName("etatActuel")
    val etatActuel: EtatMateriel?,
    val disponible: Boolean,
    val typeMaterielId: Long?,
    val typeMaterielNom: String?,
    val dateCreation: String?
)

data class AffectationMaterielResponse(
    val id: Long,
    val materielId: String,
    val materielMarque: String?,
    val materielModele: String?,
    @SerializedName("materielEtat")
    val materielEtat: EtatMateriel?,
    @SerializedName("etat")
    val etat: EtatMateriel?,
    @SerializedName("etatActuel")
    val etatActuel: EtatMateriel?,
    @SerializedName("materielDisponible")
    val materielDisponible: Boolean,
    val typeAffectation: String?,
    val departementNom: String?,
    val personnelNom: String?,
    val personnelPrenom: String?,
    val active: Boolean
)
