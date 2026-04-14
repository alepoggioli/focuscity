package com.focuscity.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.pointerInput
import com.focuscity.data.model.Building
import com.focuscity.data.model.BuildingType
import com.focuscity.ui.theme.*

@Composable
fun IsometricCityGrid(
    buildings: List<Building>,
    gridSize: Int = 50,
    selectedBuilding: Building? = null,
    isPlacing: Boolean = false,
    onCellTap: (x: Int, y: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Current pan and zoom
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // Constants for projection
    val baseTileWidth = 100f
    val baseTileHeight = baseTileWidth / 2

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.2f, 3f)
                    offsetX += pan.x
                    offsetY += pan.y
                }
            }
            .pointerInput(buildings, selectedBuilding, isPlacing, scale, offsetX, offsetY) {
                detectTapGestures { tapOffset ->
                    val tw = baseTileWidth * scale
                    val th = baseTileHeight * scale

                    // Inverse projection
                    val adjustedX = tapOffset.x - offsetX
                    val adjustedY = tapOffset.y - offsetY

                    // Standard iso to cartesian calculation
                    // x = (gX - gY) * tw/2
                    // y = (gX + gY) * th/2
                    // Thus:
                    // 2*x/tw = gX - gY
                    // 2*y/th = gX + gY
                    // 2*x/tw + 2*y/th = 2*gX => gX = x/tw + y/th
                    // 2*y/th - 2*x/tw = 2*gY => gY = y/th - x/tw

                    val gridX = (adjustedX / tw + adjustedY / th).toInt()
                    val gridY = (adjustedY / th - adjustedX / tw).toInt()

                    if (gridX in 0 until gridSize && gridY in 0 until gridSize) {
                        onCellTap(gridX, gridY)
                    }
                }
            }
    ) {
        val tw = baseTileWidth * scale
        val th = baseTileHeight * scale

        // Helper to get screen coordinates for a grid coordinate
        fun getScreenPos(gX: Float, gY: Float): Offset {
            val x = (gX - gY) * (tw / 2) + offsetX
            val y = (gX + gY) * (th / 2) + offsetY
            return Offset(x, y)
        }

        // Draw background
        drawRect(GrassGreen)

        // Draw grid lines
        for (i in 0..gridSize) {
            val start1 = getScreenPos(i.toFloat(), 0f)
            val end1 = getScreenPos(i.toFloat(), gridSize.toFloat())
            drawLine(GridLine.copy(alpha = 0.3f), start1, end1, strokeWidth = 1f)

            val start2 = getScreenPos(0f, i.toFloat())
            val end2 = getScreenPos(gridSize.toFloat(), i.toFloat())
            drawLine(GridLine.copy(alpha = 0.3f), start2, end2, strokeWidth = 1f)
        }

        // Draw placed buildings in depth order
        // Sort items by depth (x + y). Since elements are primarily ordered by (gX + gY) naturally for iso setup.
        val sortedBuildings = buildings.sortedBy { it.gridX + it.gridY }

        for (building in sortedBuildings) {
            val type = try {
                BuildingType.valueOf(building.type)
            } catch (_: Exception) { continue }

            val origin = getScreenPos(building.gridX.toFloat(), building.gridY.toFloat())
            
            // In pure isometric, a structure of WxH covers W tiles along X, H tiles along Y.
            // Screen points of the footprint:
            val pBottom = getScreenPos(building.gridX.toFloat() + type.width, building.gridY.toFloat() + type.height)
            val pLeft = getScreenPos(building.gridX.toFloat(), building.gridY.toFloat() + type.height)
            val pRight = getScreenPos(building.gridX.toFloat() + type.width, building.gridY.toFloat())

            // Height of the building (Z axis)
            val zHeight = (type.width + type.height) * 15f * scale

            val roofZ = origin.copy(y = origin.y - zHeight)
            val roofLeft = pLeft.copy(y = pLeft.y - zHeight)
            val roofRight = pRight.copy(y = pRight.y - zHeight)
            val roofBottom = pBottom.copy(y = pBottom.y - zHeight)

            // Left Wall
            val leftWallPath = Path().apply {
                moveTo(origin.x, origin.y)
                lineTo(pLeft.x, pLeft.y)
                lineTo(roofLeft.x, roofLeft.y)
                lineTo(roofZ.x, roofZ.y)
                close()
            }
            drawPath(path = leftWallPath, color = type.composeColor.copy(alpha = 0.8f))
            drawPath(path = leftWallPath, color = type.composeBorderColor, style = Stroke(width = 2f))

            // Right Wall
            val rightWallPath = Path().apply {
                moveTo(origin.x, origin.y)
                lineTo(pRight.x, pRight.y)
                lineTo(roofRight.x, roofRight.y)
                lineTo(roofZ.x, roofZ.y)
                close()
            }
            drawPath(path = rightWallPath, color = type.composeColor.copy(alpha = 0.6f))
            drawPath(path = rightWallPath, color = type.composeBorderColor, style = Stroke(width = 2f))

            // Roof
            val roofPath = Path().apply {
                moveTo(roofZ.x, roofZ.y)
                lineTo(roofLeft.x, roofLeft.y)
                lineTo(roofBottom.x, roofBottom.y)
                lineTo(roofRight.x, roofRight.y)
                close()
            }
            drawPath(path = roofPath, color = type.composeColor)
            drawPath(path = roofPath, color = type.composeBorderColor, style = Stroke(width = 2f))

            // Selection highlight
            if (selectedBuilding?.id == building.id) {
                val baseFootprint = Path().apply {
                    moveTo(origin.x, origin.y)
                    lineTo(pLeft.x, pLeft.y)
                    lineTo(pBottom.x, pBottom.y)
                    lineTo(pRight.x, pRight.y)
                    close()
                }
                drawPath(path = baseFootprint, color = Gold.copy(alpha = 0.5f), style = Fill)
            }
        }
    }
}
