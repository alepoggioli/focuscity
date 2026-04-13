package com.focuscity.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.focuscity.service.SessionType
import com.focuscity.ui.theme.*

@Composable
fun TimerDisplay(
    elapsedSeconds: Long,
    targetMinutes: Int,
    sessionType: SessionType,
    modifier: Modifier = Modifier
) {
    // Pulsing colon animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val colonAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "colonAlpha"
    )

    val displaySeconds = if (sessionType == SessionType.TIMER) {
        val remaining = (targetMinutes * 60L) - elapsedSeconds
        remaining.coerceAtLeast(0)
    } else {
        elapsedSeconds
    }

    val hours = displaySeconds / 3600
    val minutes = (displaySeconds % 3600) / 60
    val seconds = displaySeconds % 60

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Label
        Text(
            text = if (sessionType == SessionType.TIMER) "REMAINING" else "ELAPSED",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )

        Spacer(Modifier.height(8.dp))

        // Time display
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (hours > 0) {
                Text(
                    text = String.format("%d", hours),
                    style = MaterialTheme.typography.displayLarge,
                    color = VibrantPink
                )
                Text(
                    text = ":",
                    style = MaterialTheme.typography.displayLarge,
                    color = VibrantPink,
                    modifier = Modifier.alpha(colonAlpha)
                )
            }
            Text(
                text = String.format("%02d", minutes),
                style = MaterialTheme.typography.displayLarge,
                color = VibrantPink
            )
            Text(
                text = ":",
                style = MaterialTheme.typography.displayLarge,
                color = VibrantPink,
                modifier = Modifier.alpha(colonAlpha)
            )
            Text(
                text = String.format("%02d", seconds),
                style = MaterialTheme.typography.displayLarge,
                color = VibrantPink
            )
        }

        // Progress for timer mode
        if (sessionType == SessionType.TIMER) {
            Spacer(Modifier.height(4.dp))
            val total = targetMinutes * 60f
            val progress = (elapsedSeconds / total).coerceIn(0f, 1f)
            val percentage = (progress * 100).toInt()
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}
