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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
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
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val baseTileWidth = 100f
    val baseTileHeight = baseTileWidth / 2

    val textMeasurer = rememberTextMeasurer()

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

                    val adjustedX = tapOffset.x - offsetX
                    val adjustedY = tapOffset.y - offsetY

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

        fun getScreenPos(gX: Float, gY: Float): Offset {
            val x = (gX - gY) * (tw / 2) + offsetX
            val y = (gX + gY) * (th / 2) + offsetY
            return Offset(x, y)
        }

        drawRect(GrassGreen)

        for (i in 0..gridSize) {
            val start1 = getScreenPos(i.toFloat(), 0f)
            val end1 = getScreenPos(i.toFloat(), gridSize.toFloat())
            drawLine(GridLine.copy(alpha = 0.3f), start1, end1, strokeWidth = 1f)

            val start2 = getScreenPos(0f, i.toFloat())
            val end2 = getScreenPos(gridSize.toFloat(), i.toFloat())
            drawLine(GridLine.copy(alpha = 0.3f), start2, end2, strokeWidth = 1f)
        }

        val sortedBuildings = buildings.sortedBy { it.gridX + it.gridY }

        for (building in sortedBuildings) {
            val type = try {
                BuildingType.valueOf(building.type)
            } catch (_: Exception) { continue }

            val origin = getScreenPos(building.gridX.toFloat(), building.gridY.toFloat())
            val pBottom = getScreenPos(building.gridX.toFloat() + type.width, building.gridY.toFloat() + type.height)
            val pLeft = getScreenPos(building.gridX.toFloat(), building.gridY.toFloat() + type.height)
            val pRight = getScreenPos(building.gridX.toFloat() + type.width, building.gridY.toFloat())

            val zHeight = (type.width + type.height) * 15f * scale

            when (type) {
                BuildingType.SHOP -> drawShop(type, origin, pLeft, pRight, pBottom, zHeight, textMeasurer, scale)
                BuildingType.BANK -> drawBank(type, origin, pLeft, pRight, pBottom, zHeight, textMeasurer, scale)
                BuildingType.CAFE -> drawCafe(type, origin, pLeft, pRight, pBottom, zHeight, textMeasurer, scale)
                else -> drawGenericBuilding(type, origin, pLeft, pRight, pBottom, zHeight)
            }

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

// --- Specific Building Drawers ---

private fun DrawScope.drawGenericBuilding(
    type: BuildingType, origin: Offset, pLeft: Offset, pRight: Offset, pBottom: Offset, zHeight: Float
) {
    drawIsoBlock(origin, pLeft, pRight, pBottom, zHeight, type.composeColor, type.composeBorderColor)
}

private fun DrawScope.drawShop(
    type: BuildingType, origin: Offset, pLeft: Offset, pRight: Offset, pBottom: Offset, zHeight: Float, measurer: TextMeasurer, scale: Float
) {
    // Sloped roof shop with front door
    val bodyHeight = zHeight * 0.7f
    drawIsoBlock(origin, pLeft, pRight, pBottom, bodyHeight, type.composeColor.copy(alpha = 0.8f), type.composeBorderColor)

    // Overhanging Roof (Purple)
    val rOrigin = origin.copy(y = origin.y - bodyHeight)
    val rLeft = pLeft.copy(x = pLeft.x - 10f * scale, y = pLeft.y - bodyHeight + 5f * scale)
    val rRight = pRight.copy(x = pRight.x + 10f * scale, y = pRight.y - bodyHeight + 5f * scale)
    val rBottom = pBottom.copy(y = pBottom.y - bodyHeight + 10f * scale)
    val topPeak = rOrigin.copy(y = rOrigin.y - zHeight * 0.3f)

    val roofFrontLeftPath = Path().apply {
        moveTo(rLeft.x, rLeft.y); lineTo(rBottom.x, rBottom.y); lineTo(topPeak.x, topPeak.y); close()
    }
    drawPath(roofFrontLeftPath, Color(0xFF9C27B0L).copy(alpha = 0.9f))
    drawPath(roofFrontLeftPath, Color.Black, style = Stroke(2f))

    val roofFrontRightPath = Path().apply {
        moveTo(rBottom.x, rBottom.y); lineTo(rRight.x, rRight.y); lineTo(topPeak.x, topPeak.y); close()
    }
    drawPath(roofFrontRightPath, Color(0xFF9C27B0L).copy(alpha = 0.7f))
    drawPath(roofFrontRightPath, Color.Black, style = Stroke(2f))

    // Sign "SHOP"
    val centerFacade = Offset((pLeft.x + pBottom.x) / 2, (pLeft.y + pBottom.y) / 2 - bodyHeight / 2)
    drawText(
        textMeasurer = measurer,
        text = "SHOP",
        style = TextStyle(color = Color.White, fontSize = (14f * scale).sp, fontWeight = FontWeight.Bold),
        topLeft = Offset(centerFacade.x - 20f * scale, centerFacade.y - 15f * scale)
    )
}

private fun DrawScope.drawBank(
    type: BuildingType, origin: Offset, pLeft: Offset, pRight: Offset, pBottom: Offset, zHeight: Float, measurer: TextMeasurer, scale: Float
) {
    // Base steps
    val stepHeight = zHeight * 0.15f
    drawIsoBlock(origin, pLeft, pRight, pBottom, stepHeight, Color.LightGray, type.composeBorderColor)

    // Inner building (shrunken)
    val sOrigin = Offset((origin.x * 3 + pBottom.x) / 4, (origin.y * 3 + pBottom.y) / 4)
    val sBottom = Offset((pBottom.x * 3 + origin.x) / 4, (pBottom.y * 3 + origin.y) / 4)
    val sLeft = Offset((pLeft.x * 3 + pRight.x) / 4, (pLeft.y * 3 + pRight.y) / 4)
    val sRight = Offset((pRight.x * 3 + pLeft.x) / 4, (pRight.y * 3 + pLeft.y) / 4)
    val mainBodyHeight = zHeight * 0.65f
    
    val p1 = origin.copy(y = origin.y - stepHeight)
    val p2 = pLeft.copy(y = pLeft.y - stepHeight)
    val p3 = pRight.copy(y = pRight.y - stepHeight)
    val p4 = pBottom.copy(y = pBottom.y - stepHeight)
    
    // Draw columns across front-left face
    for (i in 1..4) {
        val frac = i / 5f
        val colBottom = Offset(p2.x + (p4.x - p2.x) * frac, p2.y + (p4.y - p2.y) * frac)
        val colLeft = colBottom.copy(x = colBottom.x - 5f * scale, y = colBottom.y - 2f * scale)
        val colRight = colBottom.copy(x = colBottom.x + 5f * scale, y = colBottom.y - 2f * scale)
        val colTop = colBottom.copy(y = colBottom.y - mainBodyHeight)
        drawIsoBlock(colBottom.copy(y = colBottom.y - 5f), colLeft, colRight, colBottom, mainBodyHeight, Color(0xFFEEEEEE), Color.Gray)
    }

    // Top Pediment
    val pedOrigin = p1.copy(y = p1.y - mainBodyHeight)
    val pedLeft = p2.copy(y = p2.y - mainBodyHeight)
    val pedRight = p3.copy(y = p3.y - mainBodyHeight)
    val pedBottom = p4.copy(y = p4.y - mainBodyHeight)
    drawIsoBlock(pedOrigin, pedLeft, pedRight, pedBottom, stepHeight, Color.LightGray, type.composeBorderColor)
    
    // Triangle Roof
    val pedRoofTop = pedOrigin.copy(y = pedOrigin.y - stepHeight - zHeight * 0.2f)
    val frontTri = Path().apply {
        moveTo(pedLeft.copy(y = pedLeft.y - stepHeight).x, pedLeft.copy(y = pedLeft.y - stepHeight).y)
        lineTo(pedBottom.copy(y = pedBottom.y - stepHeight).x, pedBottom.copy(y = pedBottom.y - stepHeight).y)
        lineTo(pedRoofTop.x, pedRoofTop.y)
        close()
    }
    drawPath(frontTri, Color.Gray)
    drawPath(frontTri, Color.DarkGray, style = Stroke(2f))

    val center = Offset((pedLeft.x + pedBottom.x) / 2, pedLeft.copy(y = pedLeft.y - stepHeight).y - 15f * scale)
    drawText(
        textMeasurer = measurer,
        text = "$",
        style = TextStyle(color = Gold, fontSize = (20f * scale).sp, fontWeight = FontWeight.Bold),
        topLeft = Offset(center.x - 5f * scale, center.y - 10f * scale)
    )
}

private fun DrawScope.drawCafe(
    type: BuildingType, origin: Offset, pLeft: Offset, pRight: Offset, pBottom: Offset, zHeight: Float, measurer: TextMeasurer, scale: Float
) {
    drawIsoBlock(origin, pLeft, pRight, pBottom, zHeight * 0.8f, type.composeColor, type.composeBorderColor)

    // Awning slope
    val aStartL = pLeft.copy(y = pLeft.y - zHeight * 0.8f)
    val aStartR = pBottom.copy(y = pBottom.y - zHeight * 0.8f)
    val aEndL = pLeft.copy(x = pLeft.x - 10f * scale, y = pLeft.y - zHeight * 0.6f + 5f * scale)
    val aEndR = pBottom.copy(x = pBottom.x - 10f * scale, y = pBottom.y - zHeight * 0.6f + 5f * scale)

    val awningPath = Path().apply {
        moveTo(aStartL.x, aStartL.y); lineTo(aStartR.x, aStartR.y)
        lineTo(aEndR.x, aEndR.y); lineTo(aEndL.x, aEndL.y); close()
    }
    drawPath(awningPath, Color(0xFFFFCC80))
    drawPath(awningPath, Color.Black, style = Stroke(2f))

    // Cafe text
    val centerFacade = Offset((pLeft.x + pBottom.x) / 2, (pLeft.y + pBottom.y) / 2 - zHeight * 0.4f)
    drawText(
        textMeasurer = measurer,
        text = "Café",
        style = TextStyle(color = Color.White, fontSize = (16f * scale).sp, fontWeight = FontWeight.Bold),
        topLeft = Offset(centerFacade.x - 20f * scale, centerFacade.y - 12f * scale)
    )
}

// Helper to draw a generic block
private fun DrawScope.drawIsoBlock(
    origin: Offset, pLeft: Offset, pRight: Offset, pBottom: Offset, zHeight: Float, color: Color, borderColor: Color
) {
    val roofZ = origin.copy(y = origin.y - zHeight)
    val roofLeft = pLeft.copy(y = pLeft.y - zHeight)
    val roofRight = pRight.copy(y = pRight.y - zHeight)
    val roofBottom = pBottom.copy(y = pBottom.y - zHeight)

    // Front-Left Wall
    val leftWallPath = Path().apply {
        moveTo(pLeft.x, pLeft.y); lineTo(pBottom.x, pBottom.y)
        lineTo(roofBottom.x, roofBottom.y); lineTo(roofLeft.x, roofLeft.y); close()
    }
    drawPath(leftWallPath, color, alpha = 1.0f)
    drawPath(leftWallPath, borderColor, style = Stroke(width = 2f))

    // Front-Right Wall
    val rightWallPath = Path().apply {
        moveTo(pBottom.x, pBottom.y); lineTo(pRight.x, pRight.y)
        lineTo(roofRight.x, roofRight.y); lineTo(roofBottom.x, roofBottom.y); close()
    }
    drawPath(rightWallPath, color.copy(alpha = 0.8f), alpha = 1.0f)
    drawPath(rightWallPath, borderColor, style = Stroke(width = 2f))

    // Roof
    val roofPath = Path().apply {
        moveTo(roofZ.x, roofZ.y); lineTo(roofLeft.x, roofLeft.y)
        lineTo(roofBottom.x, roofBottom.y); lineTo(roofRight.x, roofRight.y); close()
    }
    drawPath(roofPath, color)
    drawPath(roofPath, borderColor, style = Stroke(width = 2f))
}
