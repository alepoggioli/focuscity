package com.focuscity.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.focuscity.data.model.BuildingType
import com.focuscity.ui.theme.*

@Composable
fun BuildingCard(
    buildingType: BuildingType,
    coins: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val affordable = coins >= buildingType.cost
    val alpha = if (affordable) 1f else 0.5f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceLight.copy(alpha = alpha))
            .border(
                width = 2.dp,
                color = if (affordable) buildingType.composeBorderColor else TextMuted,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = affordable, onClick = onClick)
            .padding(12.dp)
    ) {
        // Building preview (colored rectangle)
        Box(
            modifier = Modifier
                .size(
                    width = (buildingType.width * 16).dp,
                    height = (buildingType.height * 16).dp
                )
                .background(buildingType.composeColor, RoundedCornerShape(2.dp))
                .border(2.dp, buildingType.composeBorderColor, RoundedCornerShape(2.dp))
        )

        Spacer(Modifier.height(8.dp))

        // Name
        Text(
            text = buildingType.displayName,
            style = MaterialTheme.typography.labelLarge,
            color = TextPrimary
        )

        // Size
        Text(
            text = "${buildingType.width}×${buildingType.height}",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )

        Spacer(Modifier.height(4.dp))

        // Cost
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "\uD83E\uDE99", fontSize = MaterialTheme.typography.bodySmall.fontSize)
            Spacer(Modifier.width(4.dp))
            Text(
                text = "${buildingType.cost}",
                style = MaterialTheme.typography.labelMedium,
                color = if (affordable) CoinGold else ErrorRed
            )
        }
    }
}
