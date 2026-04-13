package com.focuscity.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.focuscity.ui.screen.*
import com.focuscity.ui.theme.*
import com.focuscity.ui.viewmodel.SessionViewModel

// ── Routes ──────────────────────────────────────────────────

object Routes {
    const val HOME = "home"
    const val CITY = "city"
    const val STATS = "stats"
    const val SESSION_SETUP = "session_setup"
    const val ACTIVE_SESSION = "active_session"
    const val SESSION_COMPLETE = "session_complete"
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Routes.HOME, "Home", Icons.Default.Home),
    BottomNavItem(Routes.CITY, "City", Icons.Default.LocationCity),
    BottomNavItem(Routes.STATS, "Stats", Icons.Default.BarChart)
)

// ── Nav Host ────────────────────────────────────────────────

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide bottom nav during session flow
    val showBottomBar = currentRoute in listOf(Routes.HOME, Routes.CITY, Routes.STATS)

    // Share SessionViewModel across session screens
    val sessionViewModel: SessionViewModel = viewModel()

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = DarkBlue,
                    contentColor = TextPrimary
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    item.icon,
                                    contentDescription = item.label,
                                    tint = if (selected) VibrantPink else TextMuted
                                )
                            },
                            label = {
                                Text(
                                    item.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) VibrantPink else TextMuted
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = VibrantPink.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }
        },
        containerColor = DarkNavy
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            // ── Tab screens ──
            composable(Routes.HOME) {
                HomeScreen(
                    onStartSession = { navController.navigate(Routes.SESSION_SETUP) },
                    onOpenCity = { navController.navigate(Routes.CITY) }
                )
            }

            composable(Routes.CITY) {
                CityBuilderScreen()
            }

            composable(Routes.STATS) {
                StatsScreen()
            }

            // ── Session flow ──
            composable(Routes.SESSION_SETUP) {
                SessionSetupScreen(
                    onBack = { navController.popBackStack() },
                    onStartSession = {
                        navController.navigate(Routes.ACTIVE_SESSION) {
                            popUpTo(Routes.SESSION_SETUP) { inclusive = true }
                        }
                    },
                    viewModel = sessionViewModel
                )
            }

            composable(Routes.ACTIVE_SESSION) {
                ActiveSessionScreen(
                    onSessionComplete = {
                        navController.navigate(Routes.SESSION_COMPLETE) {
                            popUpTo(Routes.ACTIVE_SESSION) { inclusive = true }
                        }
                    },
                    viewModel = sessionViewModel
                )
            }

            composable(Routes.SESSION_COMPLETE) {
                SessionCompleteScreen(
                    onGoHome = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                        }
                    },
                    onGoCity = {
                        navController.navigate(Routes.CITY) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                        }
                    },
                    onStartAnother = {
                        navController.navigate(Routes.SESSION_SETUP) {
                            popUpTo(navController.graph.findStartDestination().id)
                        }
                    },
                    viewModel = sessionViewModel
                )
            }
        }
    }
}
