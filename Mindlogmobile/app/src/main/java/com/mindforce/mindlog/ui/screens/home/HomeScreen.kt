package com.mindforce.mindlog.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mindforce.mindlog.data.local.SessionManager
import com.mindforce.mindlog.data.model.PanneResponse
import com.mindforce.mindlog.data.repository.DashboardRepository
import com.mindforce.mindlog.ui.theme.*

@Composable
fun HomeScreen(
    sessionManager: SessionManager,
    dashboardRepository: DashboardRepository,
    onOpenMesSignalements: () -> Unit,
    onLogout: () -> Unit,
    onOpenProfile: () -> Unit
) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as com.mindforce.mindlog.MindForceApp
    val viewModel: HomeViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(sessionManager, dashboardRepository, app.panneRepository) as T
        }
    })

    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Scaffold(
        containerColor = MindWhite,
        topBar = {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                IconButton(
                    onClick = onLogout,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "Déconnexion",
                        tint = MindRed,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Bloc Utilisateur Fixe
            UserHeaderBlock(
                userName = state.userName,
                departementNom = state.departementNom,
                onOpenProfile = onOpenProfile
            )

            // Contenu défilant
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Tableau de bord",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MindBlack
                )

                val dashboardError = state.errorMessage
                if (!dashboardError.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MindRed.copy(alpha = 0.1f))
                    ) {
                        Text(
                            text = dashboardError,
                            color = MindRed,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    StatCard(
                        title = "Total Matériels",
                        value = state.stats?.totalMateriels?.toString() ?: "--",
                        icon = Icons.Default.Inventory2,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    StatCard(
                        title = "Total Personnels",
                        value = state.stats?.totalPersonnels?.toString() ?: "--",
                        icon = Icons.Default.Groups,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Répartition des matériels",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MindBlack
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                state.stats?.materielsParEtat?.let { etatMap ->
                    EtatDistributionGrid(etatMap)
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pannes non traitées",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MindBlack
                    )
                    TextButton(onClick = onOpenMesSignalements) {
                        Text("Voir tout", color = MindOrange)
                    }
                }
                
                if (state.untreadedPannes.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("Aucune panne en attente", color = Color.Gray)
                        }
                    }
                } else {
                    state.untreadedPannes.forEach { panne ->
                        PanneMiniCard(panne)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun UserHeaderBlock(userName: String, departementNom: String?, onOpenProfile: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .shadow(8.dp, RoundedCornerShape(24.dp))
            .clickable { onOpenProfile() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MindOrange),
        border = androidx.compose.foundation.BorderStroke(2.dp, MindBlack.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Bonjour,",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 15.sp,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = userName.ifBlank { "Chargement..." },
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            if (!departementNom.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = departementNom,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MindOrange.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MindOrange, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MindBlack)
            Text(text = title, fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun EtatDistributionGrid(etatMap: Map<String, Int>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            EtatSmallCard("Bon", etatMap["BON"] ?: 0, StateBon, Modifier.weight(1f))
            EtatSmallCard("Usage", etatMap["USAGE"] ?: 0, StateUsage, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            EtatSmallCard("En panne", etatMap["EN_PANNE"] ?: 0, StateEnPanne, Modifier.weight(1f))
            EtatSmallCard("Déclassé", etatMap["DECLASSE"] ?: 0, StateDeclasse, Modifier.weight(1f))
        }
    }
}

@Composable
fun EtatSmallCard(label: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = count.toString(), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
fun PanneMiniCard(panne: PanneResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(StateEnPanne.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Build, contentDescription = null, tint = StateEnPanne, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${panne.materielMarque ?: ""} ${panne.materielModele ?: "Inconnu"}".trim(),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MindBlack
                )
                Text(
                    text = panne.descriptionPanne,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
            }
            Text(
                text = panne.dateSignalement?.take(10) ?: "",
                fontSize = 11.sp,
                color = Color.LightGray,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
