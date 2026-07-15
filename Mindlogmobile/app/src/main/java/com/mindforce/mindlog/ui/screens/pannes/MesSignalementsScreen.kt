package com.mindforce.mindlog.ui.screens.pannes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.History
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
import com.mindforce.mindlog.data.model.PanneResponse
import com.mindforce.mindlog.data.model.StatutPanne
import com.mindforce.mindlog.data.repository.PanneRepository
import com.mindforce.mindlog.ui.components.EmptyState
import com.mindforce.mindlog.ui.components.LoadingState
import com.mindforce.mindlog.ui.components.MindForceTopBar
import com.mindforce.mindlog.ui.theme.*

@Composable
fun MesSignalementsScreen(
    repository: PanneRepository,
    onBack: (() -> Unit)? = null
) {
    val viewModel: MesSignalementsViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return MesSignalementsViewModel(repository) as T
        }
    })
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(MindWhite)) {
        MindForceTopBar(
            title = "Pannes",
            onBack = onBack
        )

        // Filter Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (state.showOnlyActive) "Pannes en cours" else "Historique",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MindBlack
            )
            
            Button(
                onClick = { viewModel.toggleFilter() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.showOnlyActive) MindBlack else MindOrange,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (state.showOnlyActive) "Historique" else "Pannes actives",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        if (state.isLoading) {
            LoadingState()
        } else if (state.errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.errorMessage!!, color = MindRed)
            }
        } else if (state.filteredPannes.isEmpty()) {
            EmptyState(message = if (state.showOnlyActive) "Aucune panne en cours" else "Votre historique est vide")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp, start = 20.dp, end = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.filteredPannes) { panne ->
                    ModernPanneCard(panne)
                }
            }
        }
    }
}

@Composable
fun ModernPanneCard(panne: PanneResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MindBlack.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, tint = MindBlack, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${panne.materielMarque ?: "Matériel"} ${panne.materielModele ?: ""}".trim(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MindBlack
                    )
                    Text(
                        text = "ID: ${panne.materielId ?: "-"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                
                PanneStatutBadge(panne.statutEtape)
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = panne.descriptionPanne,
                style = MaterialTheme.typography.bodyMedium,
                color = MindBlack,
                maxLines = 3
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Signalé le : ${panne.dateSignalement?.take(10) ?: "-"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun PanneStatutBadge(statut: StatutPanne) {
    val (label, color) = when (statut) {
        StatutPanne.SIGNALE -> "SIGNALÉE" to StateUsage
        StatutPanne.EN_REPARATION -> "RÉPARATION" to StateEnPanne
        StatutPanne.RESOLUE -> "RÉSOLUE" to StateBon
        StatutPanne.DECLASSE -> "DÉCLASSÉ" to StateDeclasse
    }

    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
