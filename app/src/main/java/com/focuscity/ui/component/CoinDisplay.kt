package com.focuscity.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.focuscity.ui.theme.*

@Composable
fun CoinDisplay(
    coins: Int,
    modifier: Modifier = Modifier,
    large: Boolean = false
) {
    val style = if (large) MaterialTheme.typography.displayMedium
                else MaterialTheme.typography.headlineMedium

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(SurfaceLight, SurfaceBright)
                )
            )
            .padding(horizontal = if (large) 20.dp else 12.dp, vertical = if (large) 12.dp else 8.dp)
    ) {
        // Pixel coin emoji
        Text(
            text = "\uD83E\uDE99",
            fontSize = if (large) style.fontSize else MaterialTheme.typography.headlineSmall.fontSize
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "$coins",
            style = style,
            color = CoinGold
        )
    }
}
