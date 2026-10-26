package com.reelcounter.app.data.preferences

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("reelcounter_prefs", Context.MODE_PRIVATE)

    fun getBubblePosition(): Pair<Int, Int> =
        Pair(prefs.getInt(KEY_X, DEFAULT_X), prefs.getInt(KEY_Y, DEFAULT_Y))

    fun saveBubblePosition(x: Int, y: Int) {
        prefs.edit().putInt(KEY_X, x).putInt(KEY_Y, y).apply()
    }

    fun resetBubblePosition() = saveBubblePosition(DEFAULT_X, DEFAULT_Y)

    fun isOnboardingCompleted(): Boolean = prefs.getBoolean(KEY_ONBOARDED, false)

    fun setOnboardingCompleted() {
        prefs.edit().putBoolean(KEY_ONBOARDED, true).apply()
    }

    private companion object {
        const val KEY_X = "bubble_x"
        const val KEY_Y = "bubble_y"
        const val KEY_ONBOARDED = "onboarded"
        const val DEFAULT_X = 40
        const val DEFAULT_Y = 400
    }
}
