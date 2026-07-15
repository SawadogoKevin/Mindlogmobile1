package com.mindforce.mindlog.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val MindForceColorScheme = lightColorScheme(
    primary = MindOrange,
    onPrimary = MindBlack,
    secondary = MindBlack,
    onSecondary = MindWhite,
    background = MindWhite,
    onBackground = MindBlack,
    surface = Color.White,
    onSurface = MindBlack,
    error = MindRed,
    onError = Color.White,
)

private val MindForceTypography = Typography(
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp, color = MindBlack),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = MindBlack),
    bodyLarge = TextStyle(fontSize = 16.sp, color = MindBlack),
    bodyMedium = TextStyle(fontSize = 14.sp, color = MindBlack),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp),
)

@Composable
fun MindForceTheme(content: @Composable () -> Unit) {
    // La charte n'impose qu'un thème clair (orange / blanc / noir)
    MaterialTheme(
        colorScheme = MindForceColorScheme,
        typography = MindForceTypography,
        content = content
    )
}
