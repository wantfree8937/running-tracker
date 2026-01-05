package com.example.runningmate.presentation

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.example.runningmate.presentation.feature.running.RunningRoute
import dagger.hilt.android.AndroidEntryPoint

// [Location]: presentation/MainActivity.kt
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Theme setup would be here
            RunningAppContent()
        }
    }
}

@Composable
fun RunningAppContent() {
    // Simple Navigation Host placeholder
    // For now, just show RunningRoute directly as per MVP
    RunningRoute(
        onNavigateToSummary = { /* Navigate to summary */ },
        onShowMessage = { message -> 
             // Toast handling needs context or should be done in Activity via SideEffect, 
             // but Route passing generic lambda is fine.
        }
    )
}
