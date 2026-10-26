package com.reelcounter.app.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.reelcounter.app.R
import com.reelcounter.app.data.preferences.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OverlayBubbleManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferenceManager: PreferenceManager
) {

    private val windowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private var bubbleView: View? = null
    private var tvReelCount: TextView? = null
    private var isBubbleShowing = false

    private lateinit var layoutParams: WindowManager.LayoutParams

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false

    @SuppressLint("ClickableViewAccessibility")
    fun showBubble(initialCount: Int = 0) {
        if (!Settings.canDrawOverlays(context)) return

        if (isBubbleShowing && bubbleView != null) {
            updateCount(initialCount)
            return
        }

        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.floating_bubble_layout, null)
        bubbleView = view
        tvReelCount = view.findViewById(R.id.tv_reel_count)
        tvReelCount?.text = initialCount.toString()

        val (savedX, savedY) = preferenceManager.getBubblePosition()

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = savedX
            y = savedY
        }

        view.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    initialTouchX = motionEvent.rawX
                    initialTouchY = motionEvent.rawY
                    isDragging = false
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val deltaX = (motionEvent.rawX - initialTouchX).toInt()
                    val deltaY = (motionEvent.rawY - initialTouchY).toInt()

                    if (Math.abs(deltaX) > 5 || Math.abs(deltaY) > 5) {
                        isDragging = true
                    }

                    layoutParams.x = initialX + deltaX
                    layoutParams.y = initialY + deltaY

                    try {
                        windowManager.updateViewLayout(bubbleView, layoutParams)
                    } catch (e: Exception) {
                        // ignore layout update race
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    preferenceManager.saveBubblePosition(layoutParams.x, layoutParams.y)
                    true
                }

                else -> false
            }
        }

        try {
            windowManager.addView(view, layoutParams)
            isBubbleShowing = true
        } catch (e: Exception) {
            e.printStackTrace()
            isBubbleShowing = false
        }
    }

    fun updateCount(count: Int) {
        tvReelCount?.text = count.toString()
    }

    fun hideBubble() {
        if (isBubbleShowing && bubbleView != null) {
            try {
                windowManager.removeView(bubbleView)
            } catch (e: Exception) {
                // ignore
            } finally {
                bubbleView = null
                tvReelCount = null
                isBubbleShowing = false
            }
        }
    }

    fun isShowing(): Boolean = isBubbleShowing
}