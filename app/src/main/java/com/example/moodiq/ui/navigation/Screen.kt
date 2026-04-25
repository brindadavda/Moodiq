package com.example.moodiq.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object Library : Screen("library", "Library", Icons.Default.LibraryMusic)
    data object Player : Screen("player", "Player", Icons.Default.PlayCircle)
    data object Favorites : Screen("favorites", "Favorites", Icons.Default.Favorite)
    data object Insights : Screen("insights", "Insights", Icons.Default.BarChart)
}
