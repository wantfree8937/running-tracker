package com.example.runningmate.presentation.feature.running

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.runningmate.presentation.feature.running.contract.RunningIntent
import com.example.runningmate.presentation.feature.running.contract.RunningState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun RunningScreen(
    state: RunningState,
    onIntent: (RunningIntent) -> Unit
) {
    var showFinishDialog by remember { mutableStateOf(false) }
    // Permission Handling
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Optionally handle permission results
    }

    LaunchedEffect(Unit) {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (permissions.any { ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED }) {
            permissionLauncher.launch(permissions)
        }
    }

    // WakeLock / Screen On Logic
    DisposableEffect(state.isRunning) {
        val window = (context as? Activity)?.window
        if (state.isRunning) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Control Buttons
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Display Coordinates
                state.currentLocation?.let { loc ->
                    Text(
                        text = "Lat: %.5f, Lng: %.5f".format(loc.latitude, loc.longitude),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(text = "Time: ${state.durationMillis / 1000} s")
                Text(text = "Distance: %.1f m".format(state.distanceMeters))
                
                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { 
                    if (state.isRunning) onIntent(RunningIntent.PauseRunning) 
                    else onIntent(RunningIntent.StartRunning) 
                }) {
                    Text(text = if (state.isRunning) "Pause" else "Start")
                }
                
                if (!state.isRunning && state.pathPoints.isNotEmpty()) {
                    Button(onClick = { showFinishDialog = true }) {
                        Text("Finish")
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            val cameraPositionState = rememberCameraPositionState()
            
            // Auto-center logic: Focus on user location when it updates
            LaunchedEffect(state.currentLocation) {
                state.currentLocation?.let { loc ->
                    cameraPositionState.animate(
                        CameraUpdateFactory.newCameraPosition(
                            CameraPosition.fromLatLngZoom(loc, 18f) // Zoom level 18 (High zoom)
                        )
                    )
                }
            }

            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                state.currentLocation?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = "Current Position"
                    )
                }
                
                if (state.pathPoints.isNotEmpty()) {
                    Polyline(
                        points = state.pathPoints,
                        color = MaterialTheme.colorScheme.primary,
                        width = 10f
                    )
                }
            }
            
            if (showFinishDialog) {
                AlertDialog(
                    onDismissRequest = { showFinishDialog = false },
                    title = { Text("Running Finished") },
                    text = {
                        Column {
                            Text("Total Time: ${state.durationMillis / 1000} s")
                            Text("Total Distance: %.1f m".format(state.distanceMeters))
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            showFinishDialog = false
                            onIntent(RunningIntent.StopRunning)
                        }) {
                            Text("Close")
                        }
                    }
                )
            }
        }
    }
}
