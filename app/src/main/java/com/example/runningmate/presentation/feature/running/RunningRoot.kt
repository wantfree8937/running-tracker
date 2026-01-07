package com.example.runningmate.presentation.feature.running

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.runningmate.presentation.feature.running.contract.RunningEffect
import com.example.runningmate.presentation.feature.running.contract.RunningIntent

// [Location]: presentation/feature/running/RunningRoot.kt
@Composable
fun RunningRoot(
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

    val context = LocalContext.current
    var showFinishDialog by remember { mutableStateOf(false) }
    var hasLocationPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.all { it }) {
            hasLocationPermission = true
            viewModel.sendIntent(RunningIntent.PermissionGranted)
        }
    }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        if (permissions.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }) {
            hasLocationPermission = true
            viewModel.sendIntent(RunningIntent.PermissionGranted)
        } else {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    RunningScreen(
        state = uiState,
        onIntent = viewModel::sendIntent,
        onNavigateToHistory = onNavigateToHistory,
        showFinishDialog = showFinishDialog,
        onShowFinishDialog = { showFinishDialog = true },
        onDismissFinishDialog = { showFinishDialog = false },
        hasLocationPermission = hasLocationPermission
    )
}
