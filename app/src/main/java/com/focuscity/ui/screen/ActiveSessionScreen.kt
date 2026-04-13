package com.focuscity.ui.screen

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
import com.focuscity.service.Difficulty
import com.focuscity.service.SessionType
import com.focuscity.ui.component.CoinDisplay
import com.focuscity.ui.component.TimerDisplay
import com.focuscity.ui.theme.*
import com.focuscity.ui.viewmodel.SessionViewModel

@Composable
fun ActiveSessionScreen(
    onSessionComplete: () -> Unit,
    viewModel: SessionViewModel = viewModel()
) {
    val state by viewModel.sessionState.collectAsState()
    var showEndDialog by remember { mutableStateOf(false) }

    // Navigate to complete screen when done
    LaunchedEffect(state.isComplete) {
        if (state.isComplete) {
            onSessionComplete()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // ── Top info ──
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(48.dp))

            // Mode badge
            val modeText = when {
                state.config.type == SessionType.TIMER && state.config.difficulty == Difficulty.EASY -> "EASY TIMER"
                state.config.type == SessionType.TIMER && state.config.difficulty == Difficulty.HARD -> "HARD TIMER"
                state.config.type == SessionType.STOPWATCH && state.config.difficulty == Difficulty.EASY -> "EASY STOPWATCH"
                else -> "HARD STOPWATCH"
            }
            val modeColor = if (state.config.difficulty == Difficulty.EASY) SoftGreen else VibrantPink

            Text(
                text = modeText,
                style = MaterialTheme.typography.labelLarge,
                color = modeColor,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(modeColor.copy(alpha = 0.15f))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        // ── Timer ──
        TimerDisplay(
            elapsedSeconds = state.elapsedSeconds,
            targetMinutes = state.config.targetMinutes,
            sessionType = state.config.type
        )

        // ── Live stats ──
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Coins earned so far
            CoinDisplay(coins = state.coinsEarned)

            // Hard mode timer: violations + forgiveness
            if (state.config.difficulty == Difficulty.HARD && state.config.type == SessionType.TIMER) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    StatChip(
                        label = "Violations",
                        value = "${state.violations}",
                        color = if (state.violations > 0) ErrorRed else TextMuted
                    )
                    StatChip(
                        label = "Forgiveness",
                        value = "${state.forgivenessRemaining}",
                        color = if (state.forgivenessRemaining <= 1) WarningAmber else SoftGreen
                    )
                }
            }

            // Hard mode hint
            if (state.config.difficulty == Difficulty.HARD) {
                Text(
                    text = if (state.config.type == SessionType.TIMER)
                        "⚠ Don't leave this app!"
                    else
                        "⚠ Leave = session ends!",
                    style = MaterialTheme.typography.bodySmall,
                    color = WarningAmber,
                    textAlign = TextAlign.Center
                )
            }
        }

        // ── End button ──
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (state.config.type == SessionType.STOPWATCH) {
                Button(
                    onClick = {
                        val minutes = (state.elapsedSeconds / 60).toInt()
                        if (minutes < 60) {
                            showEndDialog = true
                        } else {
                            showEndDialog = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SoftGreen)
                ) {
                    Text(
                        text = "FINISH SESSION",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            TextButton(
                onClick = { showEndDialog = true }
            ) {
                Text(
                    text = if (state.config.type == SessionType.TIMER) "End Early" else "Cancel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ErrorRed
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // ── End confirmation dialog ──
    if (showEndDialog) {
        val minutes = (state.elapsedSeconds / 60).toInt()
        val warningText = if (minutes >= 60) {
            "You've focused for $minutes min.\nYou'll receive 50% of earned coins."
        } else {
            "You've focused for $minutes min.\nEnding now means 0 coins."
        }

        AlertDialog(
            onDismissRequest = { showEndDialog = false },
            title = {
                Text("End Session?", style = MaterialTheme.typography.headlineSmall)
            },
            text = {
                Text(warningText, style = MaterialTheme.typography.bodyMedium)
            },
            confirmButton = {
                TextButton(onClick = {
                    showEndDialog = false
                    viewModel.stopSession()
                }) {
                    Text("End", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDialog = false }) {
                    Text("Keep Going", color = SoftGreen)
                }
            },
            containerColor = SurfaceLight
        )
    }
}

@Composable
private fun StatChip(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
    }
}
