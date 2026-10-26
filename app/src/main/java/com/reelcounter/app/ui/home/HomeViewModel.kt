package com.reelcounter.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reelcounter.app.data.local.ReelRecord
import com.reelcounter.app.data.repository.ReelRepository
import com.reelcounter.app.util.PermissionChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class HomeUiState(
    val isServiceRunning: Boolean = false,
    val todayCount: Int = 0,
    val todayDateFormatted: String = ""
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ReelRepository,
    private val permissionChecker: PermissionChecker
) : ViewModel() {

    private val serviceEnabled = MutableStateFlow(permissionChecker.isAccessibilityEnabled())

    private fun dateLabel(): String = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d"))

    val uiState: StateFlow<HomeUiState> = combine(
        repository.getTodayCountFlow(),
        serviceEnabled
    ) { count, enabled ->
        HomeUiState(isServiceRunning = enabled, todayCount = count, todayDateFormatted = dateLabel())
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        HomeUiState(isServiceRunning = serviceEnabled.value, todayDateFormatted = dateLabel())
    )

    val historyRecords: StateFlow<List<ReelRecord>> = repository.getAllHistoryFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun checkStatus() {
        serviceEnabled.value = permissionChecker.isAccessibilityEnabled()
    }

    fun openAccessibilitySettings() = permissionChecker.openAccessibilitySettings()

    fun incrementTestReel() {
        viewModelScope.launch { repository.incrementReelCount() }
    }
}
