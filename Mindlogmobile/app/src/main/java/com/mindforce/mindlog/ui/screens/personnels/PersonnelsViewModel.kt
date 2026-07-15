package com.mindforce.mindlog.ui.screens.personnels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindforce.mindlog.data.local.SessionManager
import com.mindforce.mindlog.data.model.PersonnelResponse
import com.mindforce.mindlog.data.repository.ApiResult
import com.mindforce.mindlog.data.repository.PersonnelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PersonnelsUiState(
    val isLoading: Boolean = false,
    val personnels: List<PersonnelResponse> = emptyList(),
    val errorMessage: String? = null
)

class PersonnelsViewModel(
    private val repository: PersonnelRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PersonnelsUiState())
    val uiState: StateFlow<PersonnelsUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val deptId = sessionManager.getDepartementId()
            if (deptId == null) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Département non trouvé")
                return@launch
            }

            when (val result = repository.getPersonnels(deptId)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, personnels = result.data)
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }
}
