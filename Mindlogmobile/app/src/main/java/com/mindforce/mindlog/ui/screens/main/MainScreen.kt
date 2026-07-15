package com.mindforce.mindlog.ui.screens.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mindforce.mindlog.MindForceApp
import com.mindforce.mindlog.ui.screens.home.HomeScreen
import com.mindforce.mindlog.ui.screens.materiels.MaterielsScreen
import com.mindforce.mindlog.ui.screens.pannes.MesSignalementsScreen
import com.mindforce.mindlog.ui.screens.personnels.PersonnelsScreen
import com.mindforce.mindlog.ui.screens.profil.ProfilScreen
import com.mindforce.mindlog.ui.theme.MindBlack
import com.mindforce.mindlog.ui.theme.MindOrange
import com.mindforce.mindlog.ui.theme.MindWhite

sealed class Tab(val route: String, val title: String, val icon: ImageVector) {
    object Accueil : Tab("tab_accueil", "Accueil", Icons.Default.Home)
    object Materiels : Tab("tab_materiels", "Matériels", Icons.Default.Inventory2)
    object Pannes : Tab("tab_pannes", "Pannes", Icons.Default.Engineering)
    object Personnels : Tab("tab_personnels", "Personnels", Icons.Default.Groups)
}

@Composable
fun MainScreen(
    app: MindForceApp,
    onLogout: () -> Unit,
    onMaterielClick: (String) -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val showBottomBar = currentRoute in listOf(
        Tab.Accueil.route,
        Tab.Materiels.route,
        Tab.Pannes.route,
        Tab.Personnels.route
    )

    Scaffold(
        containerColor = MindWhite,
        bottomBar = {
            if (showBottomBar) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
                ) {
                    ModernBottomNavigation(
                        currentRoute = currentRoute,
                        onTabSelected = { tab ->
                            if (currentRoute != tab.route) {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = Tab.Accueil.route
            ) {
                composable(Tab.Accueil.route) {
                    HomeScreen(
                        sessionManager = app.sessionManager,
                        dashboardRepository = app.dashboardRepository,
                        onOpenMesSignalements = { navController.navigate(Tab.Pannes.route) },
                        onLogout = onLogout,
                        onOpenProfile = { navController.navigate("profile") }
                    )
                }
                composable(Tab.Materiels.route) {
                    MaterielsScreen(
                        repository = app.materielRepository,
                        sessionManager = app.sessionManager,
                        onMaterielClick = onMaterielClick,
                        onBack = { navController.navigate(Tab.Accueil.route) }
                    )
                }
                composable(Tab.Pannes.route) {
                    MesSignalementsScreen(
                        repository = app.panneRepository,
                        onBack = { navController.navigate(Tab.Accueil.route) }
                    )
                }
                composable(Tab.Personnels.route) {
                    PersonnelsScreen(
                        repository = app.personnelRepository,
                        sessionManager = app.sessionManager,
                        onBack = { navController.navigate(Tab.Accueil.route) }
                    )
                }
                composable("profile") {
                    ProfilScreen(
                        sessionManager = app.sessionManager,
                        dashboardRepository = app.dashboardRepository,
                        onLogout = onLogout,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@Composable
fun ModernBottomNavigation(
    currentRoute: String?,
    onTabSelected: (Tab) -> Unit
) {
    val tabs = listOf(Tab.Accueil, Tab.Materiels, Tab.Pannes, Tab.Personnels)
    
    Surface(
        color = MindBlack,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .shadow(20.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val selected = currentRoute == tab.route
                BottomNavItem(
                    tab = tab,
                    selected = selected,
                    onClick = { onTabSelected(tab) }
                )
            }
        }
    }
}

@Composable
fun BottomNavItem(
    tab: Tab,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(if (selected) MindOrange else Color.Transparent, label = "bg")
    val contentColor by animateColorAsState(if (selected) MindBlack else Color.White, label = "content")
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .width(72.dp)
            .height(54.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.title,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = tab.title,
                color = contentColor,
                fontSize = 10.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
