package com.focuscity.data.model

import androidx.compose.ui.graphics.Color

enum class BuildingType(
    val displayName: String,
    val width: Int,
    val height: Int,
    val color: Long,       // Store as Long for non-Compose contexts
    val borderColor: Long,
    val cost: Int,
    val refund: Int
) {
    HALL("Hall", 3, 3, 0xFF8B4513, 0xFF808080, 0, 0),
    HOUSE("House", 2, 2, 0xFF4A90D9, 0xFF2C5F8A, 15, 7),
    SHOP("Shop", 2, 3, 0xFF5CB85C, 0xFF3D7A3D, 15, 7),
    TOWER("Tower", 1, 3, 0xFFD9534F, 0xFFA94442, 15, 7),
    PARK("Park", 3, 3, 0xFF90EE90, 0xFF8B7355, 15, 7),
    WELL("Well", 1, 1, 0xFF5BC0DE, 0xFFA0A0A0, 15, 7);

    val composeColor: Color get() = Color(color)
    val composeBorderColor: Color get() = Color(borderColor)

    companion object {
        /** All building types available for purchase (excludes Hall) */
        val purchasable: List<BuildingType>
            get() = entries.filter { it != HALL }
    }
}
