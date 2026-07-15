package com.mindforce.mindlog.ui.screens.profil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mindforce.mindlog.data.local.SessionManager
import com.mindforce.mindlog.data.repository.DashboardRepository
import com.mindforce.mindlog.ui.components.MindForceTopBar
import com.mindforce.mindlog.ui.components.DangerButton
import com.mindforce.mindlog.ui.theme.MindBlack
import com.mindforce.mindlog.ui.theme.MindOrange
import com.mindforce.mindlog.ui.theme.MindWhite

@Composable
fun ProfilScreen(
    sessionManager: SessionManager,
    dashboardRepository: DashboardRepository,
    onLogout: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val viewModel: ProfilViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return ProfilViewModel(sessionManager, dashboardRepository) as T
        }
    })
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(MindWhite)) {
        MindForceTopBar(
            title = "Mon Profil",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Profil
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MindOrange),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                    tint = MindBlack
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "${state.prenom} ${state.nom}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MindBlack
            )
            Text(
                text = state.role.replace("ROLE_", ""),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Info Cards
            ProfilInfoCard(
                icon = Icons.Default.Email,
                label = "Email",
                value = state.email
            )
            Spacer(modifier = Modifier.height(12.dp))
            ProfilInfoCard(
                icon = Icons.Default.Business,
                label = "Département",
                value = state.departement
            )
            Spacer(modifier = Modifier.height(12.dp))
            ProfilInfoCard(
                icon = Icons.Default.Badge,
                label = "Fonction",
                value = "Chef de Département" // Valeur par défaut si non dispo
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(32.dp))

            DangerButton(
                text = "Se déconnecter",
                onClick = onLogout
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ProfilInfoCard(icon: ImageVector, label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MindOrange.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MindOrange, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Text(text = value.ifBlank { "Non renseigné" }, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MindBlack)
            }
        }
    }
}
