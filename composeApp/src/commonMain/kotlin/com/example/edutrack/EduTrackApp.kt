package com.example.edutrack

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.edutrack.Onboarding.OnboardingScreen
import com.example.edutrack.Premium.PaywallScreen
import com.example.edutrack.ui.theme.EduTrackTheme

@Composable
fun EduTrackApp() {
    var route by remember { mutableStateOf(ShellRoute.Onboarding) }

    EduTrackTheme {
        when (route) {
            ShellRoute.Onboarding -> OnboardingScreen(onFinish = { _ -> route = ShellRoute.Home })
            ShellRoute.Home -> EduTrackShellHome(
                onOpenPremium = { route = ShellRoute.Paywall }
            )
            ShellRoute.Paywall -> PaywallScreen(
                onDismiss = { route = ShellRoute.Home },
                onPurchase = {}
            )
        }
    }
}

private enum class ShellRoute {
    Onboarding,
    Home,
    Paywall
}

@Composable
private fun EduTrackShellHome(onOpenPremium: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "EduTrack",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Base Kotlin Multiplatform lista para migrar los flujos principales sin romper Android.",
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onOpenPremium,
                modifier = Modifier
                    .padding(top = 28.dp)
                    .fillMaxWidth()
            ) {
                Text("Ver Premium")
            }
            TextButton(
                onClick = {},
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Android conserva el launcher actual")
            }
        }
    }
}
