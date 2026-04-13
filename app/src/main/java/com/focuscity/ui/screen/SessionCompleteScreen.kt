package com.focuscity.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focuscity.service.EndReason
import com.focuscity.ui.theme.*
import com.focuscity.ui.viewmodel.SessionViewModel

@Composable
fun SessionCompleteScreen(
    onGoHome: () -> Unit,
    onGoCity: () -> Unit,
    onStartAnother: () -> Unit,
    viewModel: SessionViewModel = viewModel()
) {
    val state by viewModel.sessionState.collectAsState()

    val emoji = when (state.endReason) {
        EndReason.COMPLETED -> "\uD83C\uDF89"
        EndReason.CANCELLED -> "\uD83D\uDE14"
        EndReason.FORGIVENESS_EXCEEDED -> "\uD83D\uDCA5"
        EndReason.APP_SWITCH -> "\uD83D\uDEAB"
        else -> "\u2705"
    }

    val title = when (state.endReason) {
        EndReason.COMPLETED -> "Session Complete!"
        EndReason.CANCELLED -> "Session Ended"
        EndReason.FORGIVENESS_EXCEEDED -> "Forgiveness Exceeded!"
        EndReason.APP_SWITCH -> "Session Stopped"
        else -> "Done"
    }

    val subtitle = when (state.endReason) {
        EndReason.COMPLETED -> "Great focus session!"
        EndReason.CANCELLED ->
            if (state.finalMinutes >= 60) "You kept some coins since you focused 60+ min."
            else "Less than 60 min — no coins this time."
        EndReason.FORGIVENESS_EXCEEDED -> "Too many app switches. Only a minimal reward."
        EndReason.APP_SWITCH -> "The stopwatch stopped because you left the app."
        else -> ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Emoji
        Text(
            text = emoji,
            style = MaterialTheme.typography.displayLarge,
            fontSize = MaterialTheme.typography.displayLarge.fontSize * 2
        )

        Spacer(Modifier.height(16.dp))

        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.displaySmall,
            color = if (state.endReason == EndReason.COMPLETED) SuccessGreen else VibrantPink,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        // Subtitle
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        // Stats row
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceLight)
                .padding(20.dp)
        ) {
            ResultStat(
                label = "Duration",
                value = "${state.finalMinutes} min",
                color = TextPrimary
            )
            ResultStat(
                label = "Coins",
                value = "+${state.finalCoins}",
                color = CoinGold
            )
            if (state.violations > 0) {
                ResultStat(
                    label = "Violations",
                    value = "${state.violations}",
                    color = ErrorRed
                )
            }
        }

        Spacer(Modifier.height(40.dp))

        // Action buttons
        Button(
            onClick = {
                viewModel.resetSession()
                onGoCity()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Gold)
        ) {
            Text(
                text = "GO TO CITY",
                style = MaterialTheme.typography.labelLarge,
                color = DarkNavy
            )
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                viewModel.resetSession()
                onStartAnother()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "START ANOTHER",
                style = MaterialTheme.typography.labelLarge,
                color = VibrantPink
            )
        }

        Spacer(Modifier.height(8.dp))

        TextButton(onClick = {
            viewModel.resetSession()
            onGoHome()
        }) {
            Text("Home", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
        }
    }
}

@Composable
private fun ResultStat(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = color
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
    }
}
