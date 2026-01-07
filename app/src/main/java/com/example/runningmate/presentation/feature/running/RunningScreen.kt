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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
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
    onIntent: (RunningIntent) -> Unit,
    onNavigateToHistory: () -> Unit,
    showFinishDialog: Boolean,
    onShowFinishDialog: () -> Unit,
    onDismissFinishDialog: () -> Unit,
    hasLocationPermission: Boolean
) {
    // Permission Handling and Dialog State moved to Root

    val context = LocalContext.current

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

        // Auto-center logic (One-time only)
        var isInitialFocus by remember { mutableStateOf(true) }
        LaunchedEffect(state.currentLocation) {
            if (isInitialFocus && state.currentLocation != null) {
                state.currentLocation?.let { loc ->
                    cameraPositionState.animate(
                        CameraUpdateFactory.newCameraPosition(
                            CameraPosition.fromLatLngZoom(loc, 17f)
                        )
                    )
                    isInitialFocus = false
                }
            }
        }

        // 1. Full Screen Map
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
            uiSettings = com.google.maps.android.compose.MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false
            ), // Hide default buttons for cleaner look
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 350.dp) // Shift focus up
        ) {
            state.currentLocation?.let {
                Marker(
                    state = MarkerState(position = it),
                    title = "Current Position"
                )
            }

            state.pathPoints.forEach { segment ->
                if (segment.isNotEmpty()) {
                    Polyline(
                        points = segment,
                        color = MaterialTheme.colorScheme.primary, // Use primary color for path
                        width = 20f
                    )
                }
            }
        }

        // --- Custom Battery Warning UI ---
        state.batteryWarning?.let { warning ->
            androidx.compose.animation.AnimatedVisibility(
                visible = true,
                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
                exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 80.dp) // Below potential status bar/top icons
                    .padding(horizontal = 24.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.9f),
                    contentColor = MaterialTheme.colorScheme.onError,
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .padding(vertical = 12.dp, horizontal = 24.dp), // Increased horizontal padding
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = warning,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp // Slightly larger font
                            )
                        )
                    }
                }
            }
        }





        // Map Controls (Zoom In/Out, My Location)
        val scope = rememberCoroutineScope()
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 400.dp, end = 16.dp) // Adjusted to be clearly above bottom sheet
        ) {
            // Zoom In
            androidx.compose.material3.SmallFloatingActionButton(
                onClick = {
                    scope.launch {
                        cameraPositionState.animate(CameraUpdateFactory.zoomIn())
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In")
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Zoom Out
            androidx.compose.material3.SmallFloatingActionButton(
                onClick = {
                    scope.launch {
                        cameraPositionState.animate(CameraUpdateFactory.zoomOut())
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
            }
            Spacer(modifier = Modifier.height(8.dp))

            // My Location
            androidx.compose.material3.SmallFloatingActionButton(
                onClick = {
                    state.currentLocation?.let { loc ->
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newCameraPosition(
                                    CameraPosition.fromLatLngZoom(loc, 17f)
                                )
                            )
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "My Location")
            }
        }

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
                // Header Row: Drag Handle + History Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp) // Fixed height to prevent jumping when History button disappears
                ) {
                    // Drag Handle (Centered)
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .width(40.dp)
                            .height(4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(2.dp)
                            )
                    )

                    // History Button (End) - Only visible when not active
                    if (!state.isRunActive) {
                        androidx.compose.material3.IconButton(
                            onClick = onNavigateToHistory,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "History",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                
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
                    text = "%.2f km".format(state.distanceMeters / 1000f),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 64.sp // Reduced from 80.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))

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

                    // Speed (Replaced Pace)
                    val speedText = "%.1f km/h".format(state.currentSpeedKmh)
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "SPEED",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = speedText,
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
                            onClick = onShowFinishDialog,
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
                onDismissRequest = onDismissFinishDialog,
                title = { Text("Finish Run?") },
                text = { Text("Are you sure you want to end this workout?") },
                confirmButton = {
                    Button(
                        onClick = {
                            onDismissFinishDialog()
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
                    TextButton(onClick = onDismissFinishDialog) {
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
