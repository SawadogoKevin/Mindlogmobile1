package com.mindforce.mindlog.ui.screens.pannes

import android.Manifest
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputEditText
import com.mindforce.mindlog.R
import com.mindforce.mindlog.data.local.SessionManager
import com.mindforce.mindlog.data.model.TypePanne
import com.mindforce.mindlog.data.repository.PanneRepository
import com.mindforce.mindlog.util.PhotoFileUtil

@Composable
fun SignalerPanneScreen(
    materielId: String,
    panneRepository: PanneRepository,
    sessionManager: SessionManager,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: SignalerPanneViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return SignalerPanneViewModel(materielId, panneRepository, sessionManager) as T
        }
    })
    val state by viewModel.uiState.collectAsState()

    var pendingCameraFile by remember { mutableStateOf<java.io.File?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val file = pendingCameraFile
        if (success && file != null) {
            val uri = PhotoFileUtil.uriForFile(context, file)
            viewModel.onPhotoReady(uri, file)
        } else {
            Toast.makeText(context, "Photo annulée", Toast.LENGTH_SHORT).show()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        uris.forEach { uri ->
            val file = PhotoFileUtil.copyContentUriToFile(context, uri)
            if (file != null) {
                viewModel.onPhotoReady(uri, file)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
        val cameraGranted = perms[Manifest.permission.CAMERA] ?: false
        if (cameraGranted) {
            val file = PhotoFileUtil.createCameraOutputFile(context)
            pendingCameraFile = file
            val uri = PhotoFileUtil.uriForFile(context, file)
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Permission caméra requise", Toast.LENGTH_SHORT).show()
        }
    }

    val galleryPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            galleryLauncher.launch("image/*")
        } else {
            Toast.makeText(context, "Permission stockage requise", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(state.success) {
        if (state.success) {
            Toast.makeText(context, "Signalement envoyé avec succès", Toast.LENGTH_LONG).show()
            onSuccess()
        }
    }

    AndroidView(
        factory = { ctx ->
            val view = LayoutInflater.from(ctx).inflate(R.layout.fragment_signaler_panne, null)
            
            val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
            toolbar.setNavigationOnClickListener { onBack() }

            val descriptionInput = view.findViewById<TextInputEditText>(R.id.descriptionInput)
            descriptionInput.doAfterTextChanged { 
                if (it.toString() != state.description) {
                    viewModel.onDescriptionChange(it.toString()) 
                }
            }

            val toggleGroup = view.findViewById<MaterialButtonToggleGroup>(R.id.typeToggleGroup)
            toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (isChecked) {
                    val type = if (checkedId == R.id.buttonReparable) TypePanne.REPARABLE else TypePanne.NON_REPARABLE
                    viewModel.onTypeChange(type)
                }
            }

            view.findViewById<Button>(R.id.cameraButton).setOnClickListener {
                permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
            }

            view.findViewById<Button>(R.id.galleryButton).setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    galleryPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                } else {
                    galleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }

            val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewPhotos)
            recyclerView.layoutManager = LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL, false)
            recyclerView.adapter = SelectedPhotoAdapter { photoItem ->
                viewModel.removePhoto(photoItem)
            }

            view.findViewById<Button>(R.id.submitButton).setOnClickListener {
                viewModel.submit()
            }

            view
        },
        update = { view ->
            view.findViewById<TextView>(R.id.materielIdText).text = state.materielId
            
            val errorBanner = view.findViewById<TextView>(R.id.errorBanner)
            errorBanner.visibility = if (state.errorMessage != null) View.VISIBLE else View.GONE
            errorBanner.text = state.errorMessage

            val descriptionInput = view.findViewById<TextInputEditText>(R.id.descriptionInput)
            if (descriptionInput.text.toString() != state.description) {
                descriptionInput.setText(state.description)
            }

            val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewPhotos)
            (recyclerView.adapter as? SelectedPhotoAdapter)?.submitList(state.photos)

            val submitButton = view.findViewById<Button>(R.id.submitButton)
            submitButton.isEnabled = !state.isSubmitting
            submitButton.text = if (state.isSubmitting) "Envoi en cours..." else "Signaler la panne"
            
            val toggleGroup = view.findViewById<MaterialButtonToggleGroup>(R.id.typeToggleGroup)
            val expectedId = if (state.typePanne == TypePanne.REPARABLE) R.id.buttonReparable else R.id.buttonNonReparable
            if (toggleGroup.checkedButtonId != expectedId) {
                toggleGroup.check(expectedId)
            }
        }
    )
}

class SelectedPhotoAdapter(private val onRemove: (PhotoItem) -> Unit) : RecyclerView.Adapter<SelectedPhotoAdapter.ViewHolder>() {
    private var items = listOf<PhotoItem>()

    fun submitList(newItems: List<PhotoItem>) {
        if (items != newItems) {
            items = newItems
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_selected_photo, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], onRemove)
    }

    override fun getItemCount() = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(photoItem: PhotoItem, onRemove: (PhotoItem) -> Unit) {
            itemView.findViewById<ImageView>(R.id.photoPreview).load(photoItem.uri)
            itemView.findViewById<ImageButton>(R.id.removeButton).setOnClickListener { onRemove(photoItem) }
        }
    }
}
