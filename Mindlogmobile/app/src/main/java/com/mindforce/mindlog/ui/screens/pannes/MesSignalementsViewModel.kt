package com.mindforce.mindlog.ui.screens.pannes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindforce.mindlog.data.model.PanneResponse
import com.mindforce.mindlog.data.model.StatutPanne
import com.mindforce.mindlog.data.repository.ApiResult
import com.mindforce.mindlog.data.repository.PanneRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MesSignalementsUiState(
    val isLoading: Boolean = false,
    val allPannes: List<PanneResponse> = emptyList(),
    val filteredPannes: List<PanneResponse> = emptyList(),
    val showOnlyActive: Boolean = true,
    val errorMessage: String? = null
)

class MesSignalementsViewModel(private val repository: PanneRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(MesSignalementsUiState())
    val uiState: StateFlow<MesSignalementsUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.getMesSignalements()) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        allPannes = result.data
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
        _uiState.value = _uiState.value.copy(showOnlyActive = !_uiState.value.showOnlyActive)
        applyFilter()
    }

    private fun applyFilter() {
        val state = _uiState.value
        val filtered = if (state.showOnlyActive) {
            state.allPannes.filter { 
                it.statutEtape == StatutPanne.SIGNALE || it.statutEtape == StatutPanne.EN_REPARATION 
            }
        } else {
            state.allPannes.filter { 
                it.statutEtape != StatutPanne.SIGNALE && it.statutEtape != StatutPanne.EN_REPARATION 
            }
        }
        _uiState.value = _uiState.value.copy(filteredPannes = filtered)
    }
}
