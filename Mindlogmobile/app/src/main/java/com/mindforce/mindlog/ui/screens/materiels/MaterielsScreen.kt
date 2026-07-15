package com.mindforce.mindlog.ui.screens.materiels

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Inventory2
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
import com.mindforce.mindlog.data.model.AffectationMaterielResponse
import com.mindforce.mindlog.data.model.EtatMateriel
import com.mindforce.mindlog.data.repository.MaterielRepository
import com.mindforce.mindlog.ui.components.EmptyState
import com.mindforce.mindlog.ui.components.LoadingState
import com.mindforce.mindlog.ui.components.MindForceTopBar
import com.mindforce.mindlog.ui.theme.*

@Composable
fun MaterielsScreen(
    repository: MaterielRepository,
    sessionManager: SessionManager,
    onMaterielClick: (String) -> Unit,
    onBack: (() -> Unit)? = null
) {
    val viewModel: MaterielsViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return MaterielsViewModel(repository, sessionManager) as T
        }
    })

    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Column(modifier = Modifier.fillMaxSize().background(MindWhite)) {
        MindForceTopBar(
            title = "Matériels",
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
                text = if (state.showOnlyAvailable) "En bon état" else "Indisponibles",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MindBlack
            )
            
            Button(
                onClick = { viewModel.toggleFilter() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.showOnlyAvailable) MindBlack else MindOrange,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (state.showOnlyAvailable) "Matériels indisponibles" else "Afficher disponibles",
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
                Text(state.errorMessage!!, color = Color.Red)
            }
        } else if (state.filteredMateriels.isEmpty()) {
            EmptyState(message = if (state.showOnlyAvailable) "Aucun matériel disponible" else "Tout le matériel est opérationnel !")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp, start = 20.dp, end = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.filteredMateriels) { item ->
                    ModernMaterielCard(item, onMaterielClick)
                }
            }
        }
    }
}

@Composable
fun ModernMaterielCard(item: AffectationMaterielResponse, onClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(item.materielId) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MindBlack.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Inventory2, contentDescription = null, tint = MindBlack, modifier = Modifier.size(26.dp))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${item.materielMarque ?: ""} ${item.materielModele ?: ""}".trim(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MindBlack
                )
                Text(
                    text = "ID: ${item.materielId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp)
                )
                val affectationLabel = when (item.typeAffectation?.uppercase()) {
                    "INDIVIDUELLE", "INDIVIDUEL" -> {
                        "${item.personnelNom ?: ""} ${item.personnelPrenom ?: ""}".trim()
                    }
                    else -> item.departementNom
                }

                if (!affectationLabel.isNullOrBlank()) {
                    Text(
                        text = affectationLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MindOrangeDark,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            EtatBadge(item.materielEtat ?: item.etat ?: item.etatActuel)
        }
    }
}

@Composable
fun EtatBadge(etat: EtatMateriel?) {
    val (label, color) = when (etat) {
        EtatMateriel.BON -> "BON" to StateBon
        EtatMateriel.USAGE -> "USAGÉ" to StateUsage
        EtatMateriel.EN_PANNE -> "PANNE" to StateEnPanne
        EtatMateriel.MAINTENANCE -> "MAINT" to StateEnPanne
        EtatMateriel.DECLASSE -> "OUT" to StateDeclasse
        EtatMateriel.HORS_SERVICE -> "HS" to StateDeclasse
        null -> "INCONNU" to Color.Gray
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
