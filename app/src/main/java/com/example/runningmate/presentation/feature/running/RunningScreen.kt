package com.example.runningmate.presentation.feature.running

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.runningmate.presentation.feature.running.contract.RunningIntent
import com.example.runningmate.presentation.feature.running.contract.RunningState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

import android.os.Build

@Composable
fun RunningScreen(
    state: RunningState,
    onIntent: (RunningIntent) -> Unit
) {
    var showFinishDialog by remember { mutableStateOf(false) }
    var hasLocationPermission by remember { mutableStateOf(false) }

    // Permission Handling
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.all { it }) {
            hasLocationPermission = true
            onIntent(RunningIntent.PermissionGranted)
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
            onIntent(RunningIntent.PermissionGranted)
        } else {
            permissionLauncher.launch(permissions.toTypedArray())
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

    Box(modifier = Modifier.fillMaxSize()) {
        val cameraPositionState = rememberCameraPositionState()

        // Auto-center logic
        LaunchedEffect(state.currentLocation) {
            state.currentLocation?.let { loc ->
                cameraPositionState.animate(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.fromLatLngZoom(loc, 17f)
                    )
                )
            }
        }

        // 1. Full Screen Map
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
            uiSettings = com.google.maps.android.compose.MapUiSettings(zoomControlsEnabled = false) // Hide default zoom controls for cleaner look
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
                    color = MaterialTheme.colorScheme.primary, // Use primary color for path
                    width = 20f
                )
            }
        }

        // 2. Bottom Stats & Controls Sheet
        // Using a Surface to simulate a bottom sheet overlaid on the map
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), // Reduced radius
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 16.dp, horizontal = 24.dp) // Reduced padding
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Drag Handle (Visual cue only)
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
                
                Spacer(modifier = Modifier.height(16.dp)) // Reduced spacer

                // --- Primary Stat: Distance ---
                Text(
                    text = "DISTANCE",
                    style = MaterialTheme.typography.labelSmall, // Smaller label
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "%.2f".format(state.distanceMeters / 1000f),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 64.sp // Reduced from 80.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "KILOMETERS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp) // Reduced padding
                )

                // --- Secondary Stats Row: Duration & Pace ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Duration
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "TIME",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatDuration(state.durationMillis),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), // Reduced style
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Pace (Calculated)
                    // Speed is km/h. Pace is min/km = 60 / speed.
                    val paceText = if (state.currentSpeedKmh > 0.1f) {
                        val pace = 60 / state.currentSpeedKmh
                        val pMin = pace.toInt()
                        val pSec = ((pace - pMin) * 60).toInt()
                        "%d'%02d\"".format(pMin, pSec)
                    } else {
                        "-'--\""
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "PACE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = paceText,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), // Reduced style
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    // Calories
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                         Text(
                            text = "KCAL",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = state.caloriesBurned.toString(),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), // Reduced style
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp)) // Reduced spacer

                // --- Controls ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (state.isRunning) {
                        // PAUSE Button
                        BigControlButton(
                            onClick = { onIntent(RunningIntent.PauseRunning) },
                            icon = Icons.Default.Pause,
                            contentDescription = "Pause",
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            iconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            size = 80.dp // Reduced from 96.dp
                        )
                    } else {
                         // RESUME / START Button
                         BigControlButton(
                            onClick = { onIntent(RunningIntent.StartRunning) },
                            icon = Icons.Default.PlayArrow,
                            contentDescription = "Start",
                            color = MaterialTheme.colorScheme.primary,
                            iconColor = MaterialTheme.colorScheme.onPrimary,
                            size = 80.dp // Reduced from 96.dp
                        )
                    }

                    // STOP Button (Only if active)
                    if (state.isRunActive && !state.isRunning) {
                        Spacer(modifier = Modifier.width(24.dp))
                        BigControlButton(
                            onClick = { showFinishDialog = true },
                            icon = Icons.Default.Stop,
                            contentDescription = "Stop",
                            color = MaterialTheme.colorScheme.error,
                            iconColor = MaterialTheme.colorScheme.onError,
                            size = 64.dp // Kept same or slightly reduced logic relative to main
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp)) // Reduced bottom spacer
            }
        }

        // Finish Dialog (Kept simple for now)
        if (showFinishDialog) {
            AlertDialog(
                onDismissRequest = { showFinishDialog = false },
                title = { Text("Finish Run?") },
                text = { Text("Are you sure you want to end this workout?") },
                confirmButton = {
                    Button(
                        onClick = {
                            showFinishDialog = false
                            onIntent(RunningIntent.StopRunning)
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Finish")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showFinishDialog = false }) {
                        Text("Resume")
                    }
                }
            )
        }
    }
}

fun formatDuration(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

@Composable
fun BigControlButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    color: Color,
    iconColor: Color,
    size: androidx.compose.ui.unit.Dp = 96.dp
) {
    Surface(
        onClick = onClick,
        shape = androidx.compose.foundation.shape.CircleShape,
        color = color,
        contentColor = iconColor,
        modifier = Modifier.size(size),
        shadowElevation = 8.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(size / 2.5f)
            )
        }
    }
}
