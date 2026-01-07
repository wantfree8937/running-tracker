package com.example.runningmate.presentation.feature.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.runningmate.data.dto.RunEntity
import kotlinx.coroutines.flow.Flow

@Composable
fun RunHistoryRoot(
    onNavigateUp: () -> Unit,
    viewModel: RunHistoryViewModel = hiltViewModel()
) {
    val runs by viewModel.runs.collectAsState(initial = emptyList())

    var showDeleteDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var runToDelete by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<RunEntity?>(null) }

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
