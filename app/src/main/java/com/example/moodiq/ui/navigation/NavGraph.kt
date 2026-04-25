package com.example.moodiq.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.moodiq.ui.screens.FavoritesScreen
import com.example.moodiq.ui.screens.HomeScreen
import com.example.moodiq.ui.screens.InsightsScreen
import com.example.moodiq.ui.screens.LibraryScreen
import com.example.moodiq.ui.screens.PlayerScreen
import com.example.moodiq.ui.theme.BackgroundBottom
import com.example.moodiq.ui.theme.BackgroundTop
import com.example.moodiq.ui.viewmodel.MainViewModel

@Composable
fun MoodiqNavGraph(viewModel: MainViewModel) {
    val navController = rememberNavController()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundTop, BackgroundBottom)))
    ) {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            bottomBar = {
                val currentDestination = navController.currentBackStackEntryAsState().value?.destination
                NavigationBar(
                    modifier = Modifier
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .clip(RoundedCornerShape(26.dp)),
                    tonalElevation = 0.dp
                ) {
                    listOf(Screen.Home, Screen.Library, Screen.Player, Screen.Favorites, Screen.Insights).forEach { screen ->
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) }
                        )
                    }
                }
            }
        ) { padding ->
            NavHost(navController, startDestination = Screen.Home.route) {
                composable(Screen.Home.route) { HomeScreen(viewModel, padding) }
                composable(Screen.Library.route) { LibraryScreen(viewModel, padding) }
                composable(Screen.Player.route) { PlayerScreen(viewModel, padding) }
                composable(Screen.Favorites.route) { FavoritesScreen(viewModel, padding) }
                composable(Screen.Insights.route) { InsightsScreen(viewModel, padding) }
            }
        }
    }
}
