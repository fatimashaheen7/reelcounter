package com.reelcounter.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reelcounter.app.data.local.ReelDao
import com.reelcounter.app.data.preferences.PreferenceManager
import com.reelcounter.app.util.PermissionChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val reelDao: ReelDao,
    private val preferenceManager: PreferenceManager,
    private val permissionChecker: PermissionChecker
) : ViewModel() {

    fun clearHistory() {
        viewModelScope.launch { reelDao.clearAll() }
    }

    fun resetBubblePosition() = preferenceManager.resetBubblePosition()

    fun openAppInfo() = permissionChecker.openAppInfo()
}
