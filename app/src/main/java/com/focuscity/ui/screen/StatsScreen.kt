package com.focuscity.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focuscity.ui.theme.*
import com.focuscity.ui.viewmodel.StatsViewModel

// Texture colors for Blueprint
val BlueprintBlue = Color(0xFF1B4E8C)
val BlueprintLine = Color.White.copy(alpha = 0.2f)
val BlueprintText = Color(0xFFE0F7FA) // Cyan-ish white

@Composable
fun StatsScreen(
    viewModel: StatsViewModel = viewModel()
) {
    val stats by viewModel.statsState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            // The Blueprint Texture
            .drawBehind {
                drawRect(BlueprintBlue)
                val cellSize = 40f
                var x = 0f
                while (x < size.width) {
                    drawLine(BlueprintLine, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                    x += cellSize
                }
                var y = 0f
                while (y < size.height) {
                    drawLine(BlueprintLine, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                    y += cellSize
                }
            }
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))

        Text(
            text = "CITY BLUEPRINT - STATS",
            style = MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Monospace),
            color = BlueprintText
        )

        Spacer(Modifier.height(32.dp))

        // Basic Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BlueprintStatCard(
                label = "Total Sessions",
                value = "${stats.totalSessions}",
                modifier = Modifier.weight(1f)
            )
            BlueprintStatCard(
                label = "Total Focus",
                value = formatMinutes(stats.totalMinutes),
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BlueprintStatCard(
                label = "Avg Session",
                value = "${stats.avgSessionMinutes.toInt()} min",
                modifier = Modifier.weight(1f)
            )
            BlueprintStatCard(
                label = "Active Days",
                value = "${stats.totalActiveDays}",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(48.dp))

        // Advanced Metrics
        Text(
            text = "ADVANCED BEHAVIOR METRICS",
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
            color = BlueprintText
        )
        
        Spacer(Modifier.height(16.dp))

        if (stats.totalActiveDays >= 7) {
            // Unlocked Advanced Stats
            AdvancedMetricsDisplay(
                avgSecondsOut = stats.avgTimeOutsideSeconds,
                avgInstancesOut = stats.avgTimeOutsideInstances
            )
        } else {
            // Locked UI
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(2.dp, BlueprintLine, RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = BlueprintText,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Unlocked after 7 days of usage.\nKeep building your city!\n(${stats.totalActiveDays}/7 days)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                        color = BlueprintText,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        if (stats.isLoading) {
            Spacer(Modifier.height(24.dp))
            CircularProgressIndicator(color = BlueprintText, strokeWidth = 2.dp)
        }

        Spacer(Modifier.height(64.dp))
    }
}

@Composable
private fun BlueprintStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .border(
                width = 2.dp,
                color = BlueprintText,
                shape = RoundedCornerShape(4.dp)
            )
            .background(Color.White.copy(alpha = 0.05f))
            .padding(16.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Monospace),
            color = Color.White
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
            color = BlueprintText,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AdvancedMetricsDisplay(avgSecondsOut: Int, avgInstancesOut: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, BlueprintText, RoundedCornerShape(4.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Bar Chart Graphic 1
        Column {
            Text(
                "Avg App Switches (Distractions)",
                style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                color = BlueprintText
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${avgInstancesOut}",
                    style = MaterialTheme.typography.headlineSmall.copy(fontFamily = FontFamily.Monospace),
                    color = Color.White
                )
                Spacer(Modifier.width(16.dp))
                Canvas(modifier = Modifier.height(20.dp).weight(1f)) {
                    val maxVal = 10f // assume max scale of 10
                    val fraction = (avgInstancesOut.toFloat() / maxVal).coerceIn(0f, 1f)
                    drawRect(
                        color = ErrorRed,
                        size = Size(size.width * fraction, size.height),
                        alpha = 0.8f
                    )
                    drawRect(
                        color = BlueprintText,
                        size = size,
                        style = Stroke(width = 2f)
                    )
                }
            }
        }

        // Bar Chart Graphic 2
        Column {
            Text(
                "Avg Time Spent Outside App",
                style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                color = BlueprintText
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${avgSecondsOut}s",
                    style = MaterialTheme.typography.headlineSmall.copy(fontFamily = FontFamily.Monospace),
                    color = Color.White
                )
                Spacer(Modifier.width(16.dp))
                Canvas(modifier = Modifier.height(20.dp).weight(1f)) {
                    val maxValSecs = 120f // assume max scale 120 seconds
                    val fraction = (avgSecondsOut.toFloat() / maxValSecs).coerceIn(0f, 1f)
                    drawRect(
                        color = WarningAmber,
                        size = Size(size.width * fraction, size.height),
                        alpha = 0.8f
                    )
                    drawRect(
                        color = BlueprintText,
                        size = size,
                        style = Stroke(width = 2f)
                    )
                }
            }
        }
    }
}

private fun formatMinutes(totalMinutes: Int): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}
