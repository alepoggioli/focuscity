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
    HALL("Hall", 3, 3, 0xFF8B4513L, 0xFF808080L, 0, 0),
    HOUSE("House", 2, 2, 0xFF4A90D9L, 0xFF2C5F8AL, 15, 7),
    SHOP("Shop", 5, 3, 0xFF9B59B6L, 0xFF8E44ADL, 25, 10),
    BANK("Bank", 4, 4, 0xFFCCCCCCL, 0xFF999999L, 40, 15),
    CAFE("Cafe", 4, 3, 0xFFD35400L, 0xFFA04000L, 30, 12),
    TOWER("Tower", 1, 3, 0xFFD9534FL, 0xFFA94442L, 15, 7),
    PARK("Park", 3, 3, 0xFF90EE90L, 0xFF8B7355L, 15, 7),
    WELL("Well", 1, 1, 0xFF5BC0DEL, 0xFFA0A0A0L, 15, 7);

    val composeColor: Color get() = Color(color)
    val composeBorderColor: Color get() = Color(borderColor)

    companion object {
        /** All building types available for purchase (excludes Hall) */
        val purchasable: List<BuildingType>
            get() = entries.filter { it != HALL }
    }
}
