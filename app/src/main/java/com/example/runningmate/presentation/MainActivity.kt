package com.example.runningmate.presentation

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.setValue
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
    val context = androidx.compose.ui.platform.LocalContext.current
    var currentScreen by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("Running") }

    when (currentScreen) {
        "Running" -> {
            com.example.runningmate.presentation.feature.running.RunningRoot(
                onNavigateToSummary = { /* Already handled by effect usually, but here we can keep as is */ },
                onNavigateToHistory = { currentScreen = "History" },
                onShowMessage = { message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            )
        }
        "History" -> {
            com.example.runningmate.presentation.feature.history.RunHistoryRoot(
                onNavigateUp = { currentScreen = "Running" }
            )
        }
    }
}
