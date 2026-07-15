package com.mindforce.mindlog.ui.screens.pannes

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindforce.mindlog.data.local.SessionManager
import com.mindforce.mindlog.data.model.TypePanne
import com.mindforce.mindlog.data.repository.ApiResult
import com.mindforce.mindlog.data.repository.PanneRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

data class PhotoItem(
    val uri: Uri,
    val file: File
)

data class SignalerPanneUiState(
    val materielId: String = "",
    val description: String = "",
    val typePanne: TypePanne = TypePanne.REPARABLE,
    val photos: List<PhotoItem> = emptyList(),
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val success: Boolean = false
)

class SignalerPanneViewModel(
    materielId: String,
    private val panneRepository: PanneRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignalerPanneUiState(materielId = materielId))
    val uiState: StateFlow<SignalerPanneUiState> = _uiState

    fun onDescriptionChange(value: String) {
        _uiState.value = _uiState.value.copy(description = value, errorMessage = null)
    }

    fun onTypeChange(value: TypePanne) {
        _uiState.value = _uiState.value.copy(typePanne = value)
    }

    /** Appelé après capture caméra ou sélection galerie, une fois le fichier local prêt */
    fun onPhotoReady(uri: Uri, file: File) {
        val currentPhotos = _uiState.value.photos.toMutableList()
        currentPhotos.add(PhotoItem(uri, file))
        _uiState.value = _uiState.value.copy(photos = currentPhotos, errorMessage = null)
    }

    fun removePhoto(photoItem: PhotoItem) {
        val currentPhotos = _uiState.value.photos.toMutableList()
        currentPhotos.remove(photoItem)
        _uiState.value = _uiState.value.copy(photos = currentPhotos)
    }

    fun submit() {
        val state = _uiState.value

        if (state.description.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Veuillez décrire la panne")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)

            val userId = sessionManager.getUserId()
            if (userId == null) {
                _uiState.value = _uiState.value.copy(isSubmitting = false, errorMessage = "Session invalide, veuillez vous reconnecter")
                return@launch
            }

            val result = panneRepository.signaler(
                materielId = state.materielId,
                typePanne = state.typePanne.name,
                description = state.description.trim(),
                userId = userId,
                photoFiles = state.photos.map { it.file }
            )

            when (result) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, success = true)
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, errorMessage = result.message)
                }
            }
        }
    }
}
