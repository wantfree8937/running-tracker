package com.example.runningmate.presentation.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runningmate.data.dto.RunEntity
import com.example.runningmate.domain.use_case.DeleteRunUseCase
import com.example.runningmate.domain.use_case.GetAllRunsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RunHistoryViewModel @Inject constructor(
    private val getAllRunsUseCase: GetAllRunsUseCase,
    private val deleteRunUseCase: DeleteRunUseCase
) : ViewModel() {

    val runs: StateFlow<List<RunEntity>> = getAllRunsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteRun(run: RunEntity) {
        viewModelScope.launch {
            deleteRunUseCase(run)
        }
    }
}
