package com.mindforce.mindlog.ui.screens.materiels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindforce.mindlog.data.model.MaterielResponse
import com.mindforce.mindlog.data.model.PanneResponse
import com.mindforce.mindlog.data.repository.ApiResult
import com.mindforce.mindlog.data.repository.MaterielRepository
import com.mindforce.mindlog.data.repository.PanneRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MaterielDetailUiState(
    val isLoading: Boolean = true,
    val materiel: MaterielResponse? = null,
    val historiquePannes: List<PanneResponse> = emptyList(),
    val errorMessage: String? = null
)

class MaterielDetailViewModel(
    private val materielId: String,
    private val materielRepository: MaterielRepository,
    private val panneRepository: PanneRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MaterielDetailUiState())
    val uiState: StateFlow<MaterielDetailUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val materielResult = materielRepository.getMateriel(materielId)
            val historiqueResult = panneRepository.getHistorique(materielId)

            if (materielResult is ApiResult.Error) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = materielResult.message)
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                materiel = (materielResult as ApiResult.Success).data,
                historiquePannes = (historiqueResult as? ApiResult.Success)?.data ?: emptyList()
            )
        }
    }
}
