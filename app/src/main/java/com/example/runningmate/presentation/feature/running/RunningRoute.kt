package com.example.runningmate.presentation.feature.running

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.runningmate.presentation.feature.running.contract.RunningEffect
import com.example.runningmate.presentation.feature.running.contract.RunningIntent

// [Location]: presentation/feature/running/RunningRoute.kt
@Composable
fun RunningRoute(
    viewModel: RunningViewModel = hiltViewModel(),
    onNavigateToSummary: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is RunningEffect.NavigateToSummary -> onNavigateToSummary()
                is RunningEffect.ShowToast -> onShowMessage(effect.message)
            }
        }
    }

    RunningScreen(
        state = uiState,
        onIntent = viewModel::sendIntent,
        onNavigateToHistory = onNavigateToHistory
    )
}
