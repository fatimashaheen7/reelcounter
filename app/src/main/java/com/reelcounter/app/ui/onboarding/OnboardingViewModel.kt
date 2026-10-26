package com.reelcounter.app.ui.onboarding

import androidx.lifecycle.ViewModel
import com.reelcounter.app.data.preferences.PreferenceManager
import com.reelcounter.app.util.PermissionChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class OnboardingUiState(
    val isAccessibilityGranted: Boolean = false,
    val isOverlayGranted: Boolean = false
) {
    val canProceed: Boolean get() = isAccessibilityGranted && isOverlayGranted
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val permissionChecker: PermissionChecker,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(readState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private fun readState() = OnboardingUiState(
        isAccessibilityGranted = permissionChecker.isAccessibilityEnabled(),
        isOverlayGranted = permissionChecker.isOverlayGranted()
    )

    fun refreshPermissionStates() {
        _uiState.value = readState()
    }

    fun openAccessibilitySettings() = permissionChecker.openAccessibilitySettings()

    fun openOverlaySettings() = permissionChecker.openOverlaySettings()

    fun completeOnboarding() = preferenceManager.setOnboardingCompleted()
}
