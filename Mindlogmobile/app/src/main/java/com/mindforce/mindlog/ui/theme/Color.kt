package com.mindforce.mindlog.ui.theme

import androidx.compose.ui.graphics.Color

// Charte graphique MindForce — à utiliser uniquement, sauf exception
// (ex : bouton logout / suppression en rouge)
val MindOrange = Color(0xFFF9AA00)
val MindWhite = Color(0xFFF8F5EE)
val MindBlack = Color(0xFF010101)

// Exceptions autorisées : actions destructrices / déconnexion
val MindRed = Color(0xFFD32F2F)
val MindRedDark = Color(0xFFB71C1C)

// Nuances utilitaires dérivées (surfaces, textes secondaires) — non imposées par la charte
val MindOrangeDark = Color(0xFFD99200)
val MindGrey = Color(0xFF6F6F6F)
val MindGreyLight = Color(0xFFE6E2D8)

// Couleurs d'état du matériel / pannes (lecture rapide, pas de rouge sauf DECLASSE)
val StateBon = Color(0xFF2E7D32)
val StateUsage = Color(0xFFF9AA00)
val StateEnPanne = Color(0xFFEF6C00)
val StateDeclasse = Color(0xFFB71C1C)
