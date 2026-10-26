package com.reelcounter.app.detector

import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import javax.inject.Inject
import javax.inject.Singleton

/**
 * =========================================================================================
 * ReelDetector
 * =========================================================================================
 * Centralized detection engine for Instagram Reels swipe transitions.
 * 
 * CORE REQUIREMENTS:
 * 1. PRIVACY GUARANTEE:
 *    - Strictly NEVER reads or stores text, captions, comments, usernames, audio tags, or messages.
 *    - Inspects only structural node types (ViewPager/RecyclerView), view IDs, and scroll events.
 * 
 * 2. 700ms DEBOUNCING:
 *    - Swiping to a new reel triggers multiple sub-events (scroll start, child layout shift,
 *      content change). A 700ms sliding window ensures exactly 1 increment per human swipe.
 * 
 * 3. PACKAGE RESTRICTION:
 *    - Operates only when the event originates from "com.instagram.android".
 * 
 * =========================================================================================
 * HOW TO UPDATE WHEN INSTAGRAM CHANGES ITS UI:
 * =========================================================================================
 * Instagram frequently obfuscates or changes its layout IDs with new app releases.
 * When Instagram updates, follow these exact 4 steps:
 * 
 * STEP 1: Connect your device via USB with USB Debugging enabled.
 * STEP 2: In Android Studio, open Tools -> Layout Inspector -> Select "com.instagram.android".
 * STEP 3: Navigate to the Reels screen in Instagram and inspect the hierarchy:
 *         - Check the resource ID of the full-screen vertical pager container.
 *         - Look for terms like "clips", "reel", "pager", "viewpager2", "vertical_scroll".
 * STEP 4: Add any new resource-id string to [KNOWN_REELS_CONTAINER_IDS] below.
 *         If Instagram switches to a new class name, update [KNOWN_REELS_CONTAINER_CLASSES].
 * =========================================================================================
 */
@Singleton
class ReelDetector @Inject constructor() {

    companion object {
        private const val TAG = "ReelDetector"
        
        /** Instagram target package */
        const val INSTAGRAM_PACKAGE = "com.instagram.android"
        
        /** 
         * Debounce window specified in requirements (700 milliseconds).
         * A typical user swipe gesture takes 250-450ms; 700ms covers settling animations
         * while allowing intentional rapid skipping.
         */
        private const val DEBOUNCE_WINDOW_MS = 700L

        /**
         * Known Instagram Reels container resource IDs across recent versions.
         * Update this list when Instagram obfuscates or renames view IDs in newer releases.
         */
        private val KNOWN_REELS_CONTAINER_IDS = listOf(
            "clips_viewer_view_pager",
            "clips_video_container",
            "clips_swipe_refresh_container",
            "reel_viewer_view_pager",
            "reel_viewer_root",
            "clips_pager",
            "clips_tab_view_pager",
            "vertical_view_pager",
            "view_pager"
        )

        /**
         * Standard Android widget classes used for full-screen paging in modern feeds.
         */
        private val KNOWN_REELS_CONTAINER_CLASSES = listOf(
            "androidx.viewpager2.widget.ViewPager2",
            "androidx.recyclerview.widget.RecyclerView",
            "com.instagram.common.ui.widget.reboundviewpager.ReboundViewPager",
            "android.widget.FrameLayout"
        )
    }

    /** Timestamp of the last verified reel swipe transition */
    private var lastTransitionTimestamp: Long = 0L

    /** Timestamp of the last TYPE_VIEW_SCROLLED event */
    private var lastScrollTimestamp: Long = 0L

    /**
     * Evaluates an incoming [AccessibilityEvent] from com.instagram.android.
     */
    @Synchronized
    fun onAccessibilityEvent(event: AccessibilityEvent?): Boolean {
        if (event == null) return false

        // 1. Verify package matches Instagram
        val eventPackage = event.packageName?.toString()
        if (eventPackage != INSTAGRAM_PACKAGE) {
            return false
        }

        val eventType = event.eventType
        val currentTime = SystemClock.uptimeMillis()

        // 2. Filter for requested event types: TYPE_VIEW_SCROLLED & TYPE_WINDOW_CONTENT_CHANGED
        when (eventType) {
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                lastScrollTimestamp = currentTime

                val isReelsScroll = isReelsContainerScrollEvent(event)
                if (isReelsScroll) {
                    return attemptTriggerReelTransition(currentTime, "TYPE_VIEW_SCROLLED")
                }
            }

            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                val timeSinceScroll = currentTime - lastScrollTimestamp
                if (timeSinceScroll in 1..400) {
                    val isReelsContent = isReelsScreenPresent(event.source)
                    if (isReelsContent) {
                        return attemptTriggerReelTransition(currentTime, "TYPE_WINDOW_CONTENT_CHANGED")
                    }
                }
            }

            else -> {
                return false
            }
        }

        return false
    }

    private fun attemptTriggerReelTransition(currentTime: Long, triggerSource: String): Boolean {
        val elapsed = currentTime - lastTransitionTimestamp
        if (elapsed < DEBOUNCE_WINDOW_MS) {
            // Debounced - belongs to the same swipe transition
            return false
        }

        lastTransitionTimestamp = currentTime
        Log.d(TAG, "New Reel detected via $triggerSource! Elapsed since last: ${elapsed}ms")
        return true
    }

    private fun isReelsContainerScrollEvent(event: AccessibilityEvent): Boolean {
        val className = event.className?.toString() ?: ""
        val isPagerClass = KNOWN_REELS_CONTAINER_CLASSES.any { className.contains(it, ignoreCase = true) }
        val isVerticalScroll = event.scrollDeltaY != 0 || event.scrollY != 0 || event.maxScrollY > 0

        val sourceNode = event.source
        var matchesReelsId = false
        if (sourceNode != null) {
            val viewId = sourceNode.viewIdResourceName?.lowercase() ?: ""
            matchesReelsId = KNOWN_REELS_CONTAINER_IDS.any { viewId.contains(it) }
            sourceNode.recycle()
        }

        return (isPagerClass && isVerticalScroll) || matchesReelsId
    }

    private fun isReelsScreenPresent(rootNode: AccessibilityNodeInfo?): Boolean {
        if (rootNode == null) return false

        try {
            for (containerId in KNOWN_REELS_CONTAINER_IDS) {
                val fullId = "$INSTAGRAM_PACKAGE:id/$containerId"
                val nodes = rootNode.findAccessibilityNodeInfosByViewId(fullId)
                if (nodes.isNotEmpty()) {
                    nodes.forEach { it.recycle() }
                    return true
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Hierarchy check error: ${e.message}")
        } finally {
            rootNode.recycle()
        }

        return false
    }

    fun resetState() {
        lastTransitionTimestamp = 0L
        lastScrollTimestamp = 0L
    }
}