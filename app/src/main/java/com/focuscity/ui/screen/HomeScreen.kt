package com.focuscity.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focuscity.ui.component.CoinDisplay
import com.focuscity.ui.theme.*
import com.focuscity.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onStartSession: () -> Unit,
    onOpenCity: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val profile by viewModel.userProfile.collectAsState()
    val todayMinutes by viewModel.todayMinutes.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // ── Header ──
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(32.dp))

            Text(
                text = "\uD83C\uDFF0",
                style = MaterialTheme.typography.displayLarge
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "FOCUSCITY",
                style = MaterialTheme.typography.displayMedium,
                color = VibrantPink
            )

            Spacer(Modifier.height(24.dp))

            // Coin display
            CoinDisplay(coins = profile.coins, large = true)

            Spacer(Modifier.height(16.dp))

            // Today's focus
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceLight)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Today: ${todayMinutes} min focused",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        // ── Actions ──
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Start Session button
            Button(
                onClick = onStartSession,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VibrantPink
                )
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "START FOCUS",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary
                )
            }

            // My City button
            OutlinedButton(
                onClick = onOpenCity,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Gold
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.horizontalGradient(listOf(Gold, CoinDarkGold))
                )
            ) {
                Icon(Icons.Default.LocationCity, contentDescription = null, tint = Gold)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "MY CITY",
                    style = MaterialTheme.typography.labelLarge,
                    color = Gold
                )
            }
        }

        // ── Stats teaser ──
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Total: ${profile.totalFocusMinutes} min",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}
