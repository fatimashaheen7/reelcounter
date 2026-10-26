package com.reelcounter.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.reelcounter.app.data.preferences.PreferenceManager
import com.reelcounter.app.ui.home.HomeScreen
import com.reelcounter.app.ui.onboarding.OnboardingScreen
import com.reelcounter.app.ui.settings.SettingsScreen
import com.reelcounter.app.ui.theme.ReelCounterTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private enum class Screen { ONBOARDING, HOME, SETTINGS }

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReelCounterTheme {
                var screen by remember {
                    mutableStateOf(
                        if (preferenceManager.isOnboardingCompleted()) Screen.HOME else Screen.ONBOARDING
                    )
                }
                when (screen) {
                    Screen.ONBOARDING -> OnboardingScreen(
                        viewModel = hiltViewModel(),
                        onNavigateToHome = { screen = Screen.HOME }
                    )
                    Screen.HOME -> HomeScreen(
                        viewModel = hiltViewModel(),
                        onNavigateToSettings = { screen = Screen.SETTINGS }
                    )
                    Screen.SETTINGS -> {
                        BackHandler { screen = Screen.HOME }
                        SettingsScreen(
                            viewModel = hiltViewModel(),
                            onBack = { screen = Screen.HOME }
                        )
                    }
                }
            }
        }
    }
}
