package com.example.wordgame.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.wordgame.AppContainer
import com.example.wordgame.navigation.Destinations
import com.example.wordgame.presentation.game.GameViewModel
import com.example.wordgame.presentation.leaderboard.LeaderboardViewModel
import com.example.wordgame.presentation.onboarding.OnboardingViewModel
import com.example.wordgame.presentation.settings.SettingsViewModel
import com.example.wordgame.ui.game.GameScreen
import com.example.wordgame.ui.leaderboard.LeaderboardScreen
import com.example.wordgame.ui.onboarding.OnboardingScreen
import com.example.wordgame.ui.settings.SettingsScreen
import kotlinx.coroutines.launch

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)


@Composable
fun WordGameApp(container: AppContainer) {
    val navController = rememberNavController()
    val backstackEntry by navController.currentBackStackEntryAsState()
    val coroutineScope = rememberCoroutineScope()
    val currentRoute = backstackEntry?.destination?.route

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(appBarTitle(currentRoute)) },
                navigationIcon = {
                    if (currentRoute != Destinations.ONBOARDING) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    container.playerPreferences.reset()
                                    navController.navigate(Destinations.ONBOARDING) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            inclusive = true
                                        }
                                        launchSingleTop = true
                                    }
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_revert),
                                contentDescription = "Start over"
                            )
                        }
                    }
                },
                actions = {
                    if (currentRoute == Destinations.GAME) {
                        IconButton(onClick = { navController.navigate(Destinations.LEADERBOARD) }) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_sort_by_size),
                                contentDescription = "Leaderboard"
                            )
                        }
                        IconButton(onClick = { navController.navigate(Destinations.SETTINGS) }) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_manage),
                                contentDescription = "Settings"
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        WordGameNavHost(
            navController = navController,
            container = container,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
private fun WordGameNavHost(
    navController: NavHostController,
    container: AppContainer,
    modifier: Modifier = Modifier
) {
    val restartScope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = Destinations.ONBOARDING,
        modifier = modifier
    ) {
        composable(Destinations.ONBOARDING) {
            val viewModel: OnboardingViewModel = viewModel(factory = container.onboardingFactory())
            val state by viewModel.state.collectAsState()
            LaunchedEffect(state.hasOnboarded) {
                if (state.hasOnboarded) {
                    navController.navigate(Destinations.GAME) {
                        popUpTo(Destinations.ONBOARDING) { inclusive = true }
                    }
                }
            }
            OnboardingScreen(
                state = state,
                onNameChanged = viewModel::onNameChanged,
                onContinue = viewModel::savePlayer
            )
        }
        composable(Destinations.GAME) {
            val viewModel: GameViewModel = viewModel(factory = container.gameFactory())
            val state by viewModel.state.collectAsState()

            GameScreen(
                state = state,
                onGuessChanged = viewModel::onGuessChanged,
                onSubmitGuess = viewModel::submitGuess,
                onRequestClue = viewModel::useClue,
                onNextLevel = viewModel::proceedToNextLevel,
                onRetryLevel = viewModel::retrySameLevel,
                onSubmitScore = viewModel::sendScoreToLeaderboard,
                onQuitGame = {
                    restartScope.launch {
                        container.playerPreferences.reset()
                        navController.navigate(Destinations.ONBOARDING) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
        composable(Destinations.LEADERBOARD) {
            val viewModel: LeaderboardViewModel = viewModel(factory = container.leaderboardFactory())
            val state by viewModel.state.collectAsState()
            LaunchedEffect(Unit) { viewModel.refresh() }
            LeaderboardScreen(state = state, onRefresh = viewModel::refresh)
        }
        composable(Destinations.SETTINGS) {
            val viewModel: SettingsViewModel = viewModel(factory = container.settingsFactory())
            val state by viewModel.state.collectAsState()
            SettingsScreen(
                state = state,
                onNameChanged = viewModel::onNameChanged,
                onSave = viewModel::saveName,
                onReset = viewModel::resetProgress
            )
        }
    }
}

private fun appBarTitle(route: String?): String = when (route) {
    Destinations.GAME -> "Fun Guessing"
    Destinations.LEADERBOARD -> "Leaderboard"
    Destinations.SETTINGS -> "Settings"
    else -> "Welcome to the Game"
}
