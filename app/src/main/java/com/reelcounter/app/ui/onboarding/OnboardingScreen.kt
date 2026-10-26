package com.reelcounter.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.reelcounter.app.ui.theme.EmeraldGreen
import com.reelcounter.app.ui.theme.TextMuted

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onNavigateToHome: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissionStates()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🎬", fontSize = 36.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Welcome to ReelCounter",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Track your daily Instagram Reels consumption with complete privacy. Two permissions are required to count swipes in real-time.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Accessibility Service Card
            PermissionCard(
                icon = Icons.Default.TouchApp,
                title = "1. Accessibility Service",
                description = "Enables ReelCounter to notice when you swipe up to the next Reel in Instagram. This service is strictly restricted to Instagram and never reads any messages, comments, captions, or usernames.",
                isGranted = uiState.isAccessibilityGranted,
                buttonText = "Open Accessibility Settings",
                onButtonClick = { viewModel.openAccessibilitySettings() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Display Over Other Apps Card
            PermissionCard(
                icon = Icons.Default.Layers,
                title = "2. Display Over Other Apps",
                description = "Allows ReelCounter to float a compact, draggable counter pill on top of Instagram so you can monitor your reel count in real time.",
                isGranted = uiState.isOverlayGranted,
                buttonText = "Open Overlay Settings",
                onButtonClick = { viewModel.openOverlaySettings() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E293B).copy(alpha = 0.6f))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "100% Offline: ReelCounter has NO internet permission. Your data never leaves your device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.completeOnboarding()
                    onNavigateToHome()
                },
                enabled = uiState.canProceed,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (uiState.canProceed) "Get Started 🚀" else "Grant Both Permissions to Continue",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
        }
    }
}