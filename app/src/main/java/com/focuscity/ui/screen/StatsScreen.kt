package com.focuscity.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focuscity.ui.component.DayDotGrid
import com.focuscity.ui.theme.*
import com.focuscity.ui.viewmodel.StatsViewModel

@Composable
fun StatsScreen(
    viewModel: StatsViewModel = viewModel()
) {
    val stats by viewModel.statsState.collectAsState()
    val ranges = listOf(7, 14, 30, 60, 90)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))

        Text(
            text = "STATISTICS",
            style = MaterialTheme.typography.headlineMedium,
            color = VibrantPink
        )

        Spacer(Modifier.height(24.dp))

        // ── Summary cards ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                label = "Avg Session",
                value = "${stats.avgSessionMinutes.toInt()} min",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Sessions/Week",
                value = String.format("%.1f", stats.avgSessionsPerWeek),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                label = "Total Sessions",
                value = "${stats.totalSessions}",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Total Focus",
                value = formatMinutes(stats.totalMinutes),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(24.dp))

        // ── Range picker ──
        Text(
            text = "FOCUS CALENDAR",
            style = MaterialTheme.typography.labelMedium,
            color = TextMuted
        )

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceLight),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ranges.forEach { range ->
                val selected = stats.selectedRange == range
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) VibrantPink.copy(alpha = 0.2f) else SurfaceLight)
                        .clickable { viewModel.setRange(range) }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "${range}d",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) VibrantPink else TextMuted
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Focus day grid ──
        if (stats.focusDays.isNotEmpty()) {
            DayDotGrid(
                days = stats.focusDays,
                label = "Last ${stats.selectedRange} days",
                modifier = Modifier.fillMaxWidth()
            )
        } else if (!stats.isLoading) {
            Text(
                text = "No data yet. Start focusing!",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
        }

        if (stats.isLoading) {
            Spacer(Modifier.height(24.dp))
            CircularProgressIndicator(color = VibrantPink, strokeWidth = 2.dp)
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceLight)
            .padding(16.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = Gold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
    }
}

private fun formatMinutes(totalMinutes: Int): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}
