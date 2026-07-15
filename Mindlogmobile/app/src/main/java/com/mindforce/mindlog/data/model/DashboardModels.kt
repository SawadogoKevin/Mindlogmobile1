package com.mindforce.mindlog.data.model

data class DashboardStats(
    val totalMateriels: Int,
    val totalPannes: Int,
    val totalAffectations: Int,
    val totalDepartements: Int,
    val totalPersonnels: Int,
    val pannesEnCours: Int,
    val alertesCritiques: Int,
    val alertesPreventives: Int,
    val materielsParEtat: Map<String, Int>?,
    val nomDepartement: String?
)
