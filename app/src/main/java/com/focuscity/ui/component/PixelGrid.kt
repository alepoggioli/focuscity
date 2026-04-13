package com.focuscity.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.focuscity.data.model.Building
import com.focuscity.data.model.BuildingType
import com.focuscity.ui.theme.*

@Composable
fun PixelGrid(
    buildings: List<Building>,
    gridSize: Int = 16,
    selectedBuilding: Building? = null,
    isPlacing: Boolean = false,
    onCellTap: (x: Int, y: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .pointerInput(buildings, selectedBuilding, isPlacing) {
                detectTapGestures { offset ->
                    val cellSize = size.width.toFloat() / gridSize
                    val x = (offset.x / cellSize).toInt().coerceIn(0, gridSize - 1)
                    val y = (offset.y / cellSize).toInt().coerceIn(0, gridSize - 1)
                    onCellTap(x, y)
                }
            }
    ) {
        val cellSize = size.width / gridSize

        // ── Background (grass pattern) ──
        drawRect(GrassGreen)

        // Alternating grass tiles for texture
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                if ((row + col) % 2 == 0) {
                    drawRect(
                        color = GrassLight,
                        topLeft = Offset(col * cellSize, row * cellSize),
                        size = Size(cellSize, cellSize)
                    )
                }
            }
        }

        // ── Grid lines ──
        for (i in 0..gridSize) {
            val pos = i * cellSize
            drawLine(
                color = GridLine.copy(alpha = 0.3f),
                start = Offset(pos, 0f),
                end = Offset(pos, size.height),
                strokeWidth = 1f
            )
            drawLine(
                color = GridLine.copy(alpha = 0.3f),
                start = Offset(0f, pos),
                end = Offset(size.width, pos),
                strokeWidth = 1f
            )
        }

        // ── Buildings ──
        for (building in buildings) {
            val type = try {
                BuildingType.valueOf(building.type)
            } catch (_: Exception) { continue }

            val x = building.gridX * cellSize
            val y = building.gridY * cellSize
            val w = type.width * cellSize
            val h = type.height * cellSize

            // Body
            drawRect(
                color = type.composeColor,
                topLeft = Offset(x, y),
                size = Size(w, h)
            )

            // Border
            drawRect(
                color = type.composeBorderColor,
                topLeft = Offset(x, y),
                size = Size(w, h),
                style = Stroke(width = 3f)
            )

            // Inner detail (small pixel window/door pattern)
            val detailSize = cellSize * 0.3f
            val centerX = x + w / 2 - detailSize / 2
            val centerY = y + h / 2 - detailSize / 2
            drawRect(
                color = type.composeBorderColor.copy(alpha = 0.6f),
                topLeft = Offset(centerX, centerY),
                size = Size(detailSize, detailSize)
            )

            // Selection highlight
            if (selectedBuilding?.id == building.id) {
                drawRect(
                    color = Gold.copy(alpha = 0.5f),
                    topLeft = Offset(x, y),
                    size = Size(w, h),
                    style = Stroke(width = 4f)
                )
            }
        }

        // ── Placement mode hint ──
        if (isPlacing) {
            // Subtle overlay on empty cells
            drawRect(
                color = Color.White.copy(alpha = 0.05f)
            )
        }
    }
}
