package com.focuscity.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.focuscity.ui.theme.*

@Composable
fun DayDotGrid(
    days: List<Boolean>,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )

        Spacer(Modifier.height(8.dp))

        // Grid of dots, 7 columns (days of week)
        val rows = days.chunked(7)
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            for (row in rows) {
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    for (focused in row) {
                        val dotSize = when {
                            days.size <= 14 -> 20.dp
                            days.size <= 30 -> 14.dp
                            else -> 10.dp
                        }
                        Box(
                            modifier = Modifier
                                .size(dotSize)
                                .clip(CircleShape)
                                .background(
                                    if (focused) SuccessGreen else SurfaceLight
                                )
                        )
                    }
                }
            }
        }

        // Summary
        val focusedCount = days.count { it }
        Spacer(Modifier.height(4.dp))
        Text(
            text = "$focusedCount / ${days.size} days",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )
    }
}
