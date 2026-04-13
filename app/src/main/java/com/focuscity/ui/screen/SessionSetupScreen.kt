package com.focuscity.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focuscity.engine.CoinCalculator
import com.focuscity.service.Difficulty
import com.focuscity.service.SessionType
import com.focuscity.ui.theme.*
import com.focuscity.ui.viewmodel.SessionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionSetupScreen(
    onBack: () -> Unit,
    onStartSession: () -> Unit,
    viewModel: SessionViewModel = viewModel()
) {
    val setup by viewModel.setupState.collectAsState()
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Session Setup", style = MaterialTheme.typography.titleLarge)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkNavy,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = DarkNavy
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Mode Toggle: Timer / Stopwatch ──
            SectionLabel("MODE")
            ToggleRow(
                option1 = "TIMER",
                option2 = "STOPWATCH",
                selected = if (setup.type == SessionType.TIMER) 0 else 1,
                onSelect = {
                    viewModel.setType(if (it == 0) SessionType.TIMER else SessionType.STOPWATCH)
                }
            )

            // ── Difficulty Toggle: Easy / Hard ──
            SectionLabel("DIFFICULTY")
            ToggleRow(
                option1 = "EASY",
                option2 = "HARD",
                selected = if (setup.difficulty == Difficulty.EASY) 0 else 1,
                onSelect = {
                    viewModel.setDifficulty(if (it == 0) Difficulty.EASY else Difficulty.HARD)
                },
                color1 = SoftGreen,
                color2 = VibrantPink
            )

            // ── Duration (Timer only) ──
            if (setup.type == SessionType.TIMER) {
                SectionLabel("DURATION")

                // Slider
                Slider(
                    value = setup.targetMinutes.toFloat(),
                    onValueChange = { viewModel.setTargetMinutes(it.toInt()) },
                    valueRange = CoinCalculator.MIN_SESSION_MINUTES.toFloat()..CoinCalculator.MAX_EASY_MINUTES.toFloat(),
                    steps = (CoinCalculator.MAX_EASY_MINUTES - CoinCalculator.MIN_SESSION_MINUTES) / CoinCalculator.TIMER_STEP - 1,
                    colors = SliderDefaults.colors(
                        thumbColor = VibrantPink,
                        activeTrackColor = VibrantPink,
                        inactiveTrackColor = SurfaceBright
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Text input for custom value
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = setup.durationText,
                        onValueChange = { viewModel.setDurationText(it) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                viewModel.commitDurationText()
                                focusManager.clearFocus()
                            }
                        ),
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            textAlign = TextAlign.Center,
                            color = TextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier.width(100.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VibrantPink,
                            unfocusedBorderColor = SurfaceBright,
                            cursorColor = VibrantPink
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("min", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
                }
            }

            // ── Forgiveness (Hard Timer only) ──
            AnimatedVisibility(
                visible = setup.difficulty == Difficulty.HARD && setup.type == SessionType.TIMER
            ) {
                Column {
                    SectionLabel("FORGIVENESS")

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        (0..CoinCalculator.MAX_FORGIVENESS).forEach { count ->
                            val selected = setup.maxForgiveness == count
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) VibrantPink else SurfaceLight)
                                    .border(
                                        2.dp,
                                        if (selected) VibrantPink else SurfaceBright,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.setMaxForgiveness(count) }
                            ) {
                                Text(
                                    text = "$count",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // ── Info Card ──
            InfoCard(setup.type, setup.difficulty)

            // ── Estimated Coins ──
            if (setup.type == SessionType.TIMER) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Est. reward: ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = "\uD83E\uDE99 ${viewModel.estimatedCoins()}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = CoinGold
                    )
                }
            }

            // ── Start Button ──
            Button(
                onClick = {
                    if (setup.type == SessionType.TIMER) viewModel.commitDurationText()
                    viewModel.startSession()
                    onStartSession()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VibrantPink)
            ) {
                Text(
                    text = "START SESSION",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Helpers ─────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = TextMuted,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun ToggleRow(
    option1: String,
    option2: String,
    selected: Int,
    onSelect: (Int) -> Unit,
    color1: androidx.compose.ui.graphics.Color = SkyBlue,
    color2: androidx.compose.ui.graphics.Color = Amber
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceLight)
    ) {
        listOf(option1 to color1, option2 to color2).forEachIndexed { index, (label, color) ->
            val isSelected = selected == index
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) color.copy(alpha = 0.2f) else SurfaceLight)
                    .border(
                        width = if (isSelected) 2.dp else 0.dp,
                        color = if (isSelected) color else SurfaceLight,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelect(index) }
                    .padding(vertical = 14.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) color else TextMuted
                )
            }
        }
    }
}

@Composable
private fun InfoCard(type: SessionType, difficulty: Difficulty) {
    val description = when {
        type == SessionType.TIMER && difficulty == Difficulty.EASY ->
            "Timer counts down. You can use other apps freely. Max 180 min."
        type == SessionType.TIMER && difficulty == Difficulty.HARD ->
            "Timer counts down. Leaving the app costs a forgiveness. Exceed the limit and the session ends with minimal reward. Each violation = -10% coins."
        type == SessionType.STOPWATCH && difficulty == Difficulty.EASY ->
            "Stopwatch counts up to 180 min. Use other apps freely."
        type == SessionType.STOPWATCH && difficulty == Difficulty.HARD ->
            "Stopwatch counts up with no limit. But leave the app and the session ends immediately."
        else -> ""
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MidBlue.copy(alpha = 0.5f))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            Icons.Default.Info,
            contentDescription = null,
            tint = SkyBlue,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}
