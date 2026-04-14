package com.focuscity.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.focuscity.ui.screen.*
import com.focuscity.ui.theme.DarkNavy
import com.focuscity.ui.viewmodel.SessionViewModel

object Routes {
    const val HOME = "home"
    const val SESSION_SETUP = "session_setup"
    const val ACTIVE_SESSION = "active_session"
    const val SESSION_COMPLETE = "session_complete"
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    // Share SessionViewModel across session screens
    val sessionViewModel: SessionViewModel = viewModel()

    Scaffold(
        containerColor = DarkNavy
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onStartSession = { navController.navigate(Routes.SESSION_SETUP) }
                )
            }

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
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onGoCity = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onStartAnother = {
                        navController.navigate(Routes.SESSION_SETUP) {
                            popUpTo(0)
                        }
                    },
                    viewModel = sessionViewModel
                )
            }
        }
    }
}
