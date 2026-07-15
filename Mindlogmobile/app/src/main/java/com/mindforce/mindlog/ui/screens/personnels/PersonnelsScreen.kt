package com.mindforce.mindlog.ui.screens.personnels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mindforce.mindlog.data.local.SessionManager
import com.mindforce.mindlog.data.model.PersonnelResponse
import com.mindforce.mindlog.data.repository.PersonnelRepository
import com.mindforce.mindlog.ui.components.EmptyState
import com.mindforce.mindlog.ui.components.LoadingState
import com.mindforce.mindlog.ui.components.MindForceTopBar
import com.mindforce.mindlog.ui.theme.*

@Composable
fun PersonnelsScreen(
    repository: PersonnelRepository,
    sessionManager: SessionManager,
    onBack: (() -> Unit)? = null
) {
    val viewModel: PersonnelsViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return PersonnelsViewModel(repository, sessionManager) as T
        }
    })
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Column(modifier = Modifier.fillMaxSize().background(MindWhite)) {
        MindForceTopBar(
            title = "Personnels",
            onBack = onBack
        )

        if (state.isLoading) {
            LoadingState()
        } else if (state.errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.errorMessage!!, color = MindRed, fontWeight = FontWeight.Bold)
            }
        } else if (state.personnels.isEmpty()) {
            EmptyState(message = "Aucun personnel dans votre département")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp, start = 20.dp, end = 20.dp, top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.personnels) { personnel ->
                    ModernPersonnelCard(personnel)
                }
            }
        }
    }
}

@Composable
fun ModernPersonnelCard(personnel: PersonnelResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Initials or Icon
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MindOrange.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${personnel.prenom.take(1)}${personnel.nom.take(1)}".uppercase(),
                    color = MindOrangeDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${personnel.prenom} ${personnel.nom}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MindBlack
                )
                Text(
                    text = "Matricule: ${personnel.matricule ?: "-"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp)
                )
                
                if (!personnel.poste.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Badge, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = personnel.poste, style = MaterialTheme.typography.bodySmall, color = MindBlack.copy(alpha = 0.7f))
                    }
                }
                
                if (!personnel.telephone.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = personnel.telephone, style = MaterialTheme.typography.bodySmall, color = MindBlack.copy(alpha = 0.7f))
                    }
                }
            }
            
            // Status Dot
            StatusBadge(personnel.actif)
        }
    }
}

@Composable
fun StatusBadge(isActive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(if (isActive) Color(0xFF4CAF50) else Color(0xFFF44336))
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (isActive) "Actif" else "Inactif",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) Color(0xFF4CAF50) else Color(0xFFF44336)
        )
    }
}
