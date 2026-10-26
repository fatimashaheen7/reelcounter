package com.reelcounter.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.reelcounter.app.service.ReelCounterAccessibilityService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionChecker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun isAccessibilityEnabled(): Boolean {
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val me = "${context.packageName}/${ReelCounterAccessibilityService::class.java.name}"
        return enabled.split(':').any { it.equals(me, ignoreCase = true) }
    }

    fun isOverlayGranted(): Boolean = Settings.canDrawOverlays(context)

    fun openAccessibilitySettings() {
        context.startActivity(
            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    fun openOverlaySettings() {
        context.startActivity(
            Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    fun openAppInfo() {
        context.startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:${context.packageName}")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
