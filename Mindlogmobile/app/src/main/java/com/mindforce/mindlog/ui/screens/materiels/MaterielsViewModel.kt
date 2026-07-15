package com.mindforce.mindlog.ui.screens.materiels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindforce.mindlog.data.local.SessionManager
import com.mindforce.mindlog.data.model.AffectationMaterielResponse
import com.mindforce.mindlog.data.model.EtatMateriel
import com.mindforce.mindlog.data.repository.ApiResult
import com.mindforce.mindlog.data.repository.MaterielRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MaterielsUiState(
    val isLoading: Boolean = false,
    val allMateriels: List<AffectationMaterielResponse> = emptyList(),
    val filteredMateriels: List<AffectationMaterielResponse> = emptyList(),
    val showOnlyAvailable: Boolean = true,
    val errorMessage: String? = null
)

class MaterielsViewModel(
    private val repository: MaterielRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MaterielsUiState())
    val uiState: StateFlow<MaterielsUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val deptId = sessionManager.getDepartementId()
            if (deptId == null || deptId == 0L) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Département non assigné")
                return@launch
            }

            when (val result = repository.getMesMateriels(deptId)) {
                is ApiResult.Success -> {
                    val materiels = result.data
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        allMateriels = materiels
                    )
                    applyFilter()
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun toggleFilter() {
        _uiState.value = _uiState.value.copy(showOnlyAvailable = !_uiState.value.showOnlyAvailable)
        applyFilter()
    }

    private fun applyFilter() {
        val state = _uiState.value
        val all = state.allMateriels
        
        val filtered = if (state.showOnlyAvailable) {
            // "En bon état" : On affiche ce qui n'est pas une panne connue
            all.filter { item ->
                val etat = item.materielEtat ?: item.etat ?: item.etatActuel
                etat != EtatMateriel.EN_PANNE && 
                etat != EtatMateriel.MAINTENANCE && 
                etat != EtatMateriel.DECLASSE && 
                etat != EtatMateriel.HORS_SERVICE
            }
        } else {
            // "Indisponibles" : On affiche uniquement ce qui est en panne, OU ce qui est marqué indisponible si l'état est inconnu
            all.filter { item ->
                val etat = item.materielEtat ?: item.etat ?: item.etatActuel
                if (etat != null) {
                    etat == EtatMateriel.EN_PANNE || 
                    etat == EtatMateriel.MAINTENANCE || 
                    etat == EtatMateriel.DECLASSE || 
                    etat == EtatMateriel.HORS_SERVICE
                } else {
                    !item.materielDisponible
                }
            }
        }
        _uiState.value = _uiState.value.copy(filteredMateriels = filtered)
    }
}
