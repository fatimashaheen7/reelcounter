package com.reelcounter.app.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.reelcounter.app.data.repository.ReelRepository
import com.reelcounter.app.detector.ReelDetector
import com.reelcounter.app.overlay.OverlayBubbleManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReelCounterAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var reelDetector: ReelDetector

    @Inject
    lateinit var reelRepository: ReelRepository

    @Inject
    lateinit var overlayBubbleManager: OverlayBubbleManager

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var isInstagramForeground = false
    private var cachedTodayCount = 0

    companion object {
        private const val TAG = "ReelCounterService"
        var isServiceRunning = false
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceRunning = true
        Log.i(TAG, "ReelCounterAccessibilityService connected.")

        serviceScope.launch {
            reelRepository.getTodayCountFlow().collectLatest { count ->
                cachedTodayCount = count
                if (isInstagramForeground && overlayBubbleManager.isShowing()) {
                    overlayBubbleManager.updateCount(count)
                }
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val packageName = event.packageName?.toString()

        // Manage Bubble visibility when Instagram enters or exits foreground
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (packageName == ReelDetector.INSTAGRAM_PACKAGE) {
                if (!isInstagramForeground) {
                    isInstagramForeground = true
                    overlayBubbleManager.showBubble(cachedTodayCount)
                }
            } else if (packageName != null) {
                if (isInstagramForeground) {
                    isInstagramForeground = false
                    overlayBubbleManager.hideBubble()
                    reelDetector.resetState()
                }
            }
        }

        if (packageName != ReelDetector.INSTAGRAM_PACKAGE) {
            return
        }

        val isNewReel = reelDetector.onAccessibilityEvent(event)
        if (isNewReel) {
            serviceScope.launch {
                val updatedCount = reelRepository.incrementReelCount()
                cachedTodayCount = updatedCount
                overlayBubbleManager.updateCount(updatedCount)
                Log.d(TAG, "Incremented count to: $updatedCount")
            }
        }
    }

    override fun onInterrupt() {
        overlayBubbleManager.hideBubble()
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        overlayBubbleManager.hideBubble()
        serviceScope.cancel()
    }
}