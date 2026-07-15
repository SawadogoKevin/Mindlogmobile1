package com.mindforce.mindlog.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mindforce.mindlog.MindForceApp
import com.mindforce.mindlog.ui.screens.login.LoginScreen
import com.mindforce.mindlog.ui.screens.main.MainScreen
import com.mindforce.mindlog.ui.screens.materiels.MaterielDetailScreen
import com.mindforce.mindlog.ui.screens.pannes.SignalerPanneScreen
import kotlinx.coroutines.flow.first
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

private object Routes {
    const val LOGIN = "login"
    const val MAIN = "main"
    const val MATERIEL_DETAIL = "materiel/{materielId}"
    const val SIGNALER_PANNE = "signaler/{materielId}"

    fun materielDetail(id: String) = "materiel/$id"
    fun signalerPanne(id: String) = "signaler/$id"
}

@Composable
fun MindForceNavGraph(app: MindForceApp) {
    val navController: NavHostController = rememberNavController()

    // Détermine l'écran de départ selon qu'une session existe déjà
    var startDestination by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        val token = app.sessionManager.tokenFlow.first()
        startDestination = if (!token.isNullOrBlank()) Routes.MAIN else Routes.LOGIN
    }

    if (startDestination == null) return // léger écran vide pendant la résolution de session

    NavHost(navController = navController, startDestination = startDestination!!) {

        composable(Routes.LOGIN) {
            LoginScreen(
                authRepository = app.authRepository,
                onLoginSuccess = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MAIN) {
            val scope = rememberCoroutineScope()
            MainScreen(
                app = app,
                onLogout = {
                    scope.launch {
                        app.authRepository.logout()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onMaterielClick = { id -> navController.navigate(Routes.materielDetail(id)) }
            )
        }

        composable(Routes.MATERIEL_DETAIL) { backStackEntry ->
            val materielId = backStackEntry.arguments?.getString("materielId") ?: return@composable
            MaterielDetailScreen(
                materielId = materielId,
                materielRepository = app.materielRepository,
                panneRepository = app.panneRepository,
                onBack = { navController.popBackStack() },
                onSignalerPanne = { id -> navController.navigate(Routes.signalerPanne(id)) }
            )
        }

        composable(Routes.SIGNALER_PANNE) { backStackEntry ->
            val materielId = backStackEntry.arguments?.getString("materielId") ?: return@composable
            SignalerPanneScreen(
                materielId = materielId,
                panneRepository = app.panneRepository,
                sessionManager = app.sessionManager,
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }
    }
}
