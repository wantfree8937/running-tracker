package com.example.runningmate.presentation.feature.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.runningmate.data.dto.RunEntity
import kotlinx.coroutines.flow.Flow

@Composable
fun RunHistoryRoot(
    onNavigateUp: () -> Unit,
    viewModel: RunHistoryViewModel = hiltViewModel()
) {
    val runs by viewModel.runs.collectAsState(initial = emptyList())

    var showDeleteDialog by remember { mutableStateOf(false) }
    var runToDelete by remember { mutableStateOf<RunEntity?>(null) }

    RunHistoryScreen(
        runs = runs,
        onRunDeleteClick = { run ->
            runToDelete = run
            showDeleteDialog = true
        },
        onNavigateUp = onNavigateUp,
        showDeleteDialog = showDeleteDialog,
        onDismissDeleteDialog = {
            showDeleteDialog = false
            runToDelete = null
        },
        onConfirmDelete = {
            runToDelete?.let { viewModel.deleteRun(it) }
            showDeleteDialog = false
            runToDelete = null
        }
    )
}
