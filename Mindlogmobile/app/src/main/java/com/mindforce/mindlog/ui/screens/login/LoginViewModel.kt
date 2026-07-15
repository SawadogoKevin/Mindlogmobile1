package com.mindforce.mindlog.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindforce.mindlog.data.repository.ApiResult
import com.mindforce.mindlog.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class LoginStep { CREDENTIALS, VERIFICATION }

data class LoginUiState(
    val step: LoginStep = LoginStep.CREDENTIALS,
    val email: String = "",
    val password: String = "",
    val code: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val loginSuccess: Boolean = false
)

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errorMessage = null)
    }

    fun onCodeChange(value: String) {
        _uiState.value = _uiState.value.copy(code = value, errorMessage = null)
    }

    /** Étape 1 : envoie email + mot de passe -> déclenche l'envoi du code 2FA */
    fun submitCredentials() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Veuillez renseigner votre email et votre mot de passe")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = authRepository.login(state.email.trim(), state.password)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        step = LoginStep.VERIFICATION,
                        infoMessage = result.data
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    /** Étape 2 : vérifie le code reçu par email -> ouvre la session */
    fun submitVerification() {
        val state = _uiState.value
        if (state.code.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Veuillez saisir le code reçu par email")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = authRepository.verify(state.email.trim(), state.code.trim())) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, loginSuccess = true)
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun backToCredentials() {
        _uiState.value = _uiState.value.copy(step = LoginStep.CREDENTIALS, code = "", errorMessage = null)
    }
}
