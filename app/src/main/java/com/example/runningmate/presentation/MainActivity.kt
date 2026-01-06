package com.example.runningmate.presentation

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
    var currentScreen by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("Running") }

    when (currentScreen) {
        "Running" -> {
            RunningRoute(
                onNavigateToSummary = { /* Already handled by effect usually, but here we can keep as is */ },
                onNavigateToHistory = { currentScreen = "History" },
                onShowMessage = { }
            )
        }
        "History" -> {
            com.example.runningmate.presentation.feature.history.RunHistoryScreen(
                onNavigateUp = { currentScreen = "Running" }
            )
        }
    }
}
