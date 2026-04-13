package com.focuscity.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "building")
data class Building(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,  // BuildingType enum name
    val gridX: Int,
    val gridY: Int,
    val placedAt: Long = System.currentTimeMillis()
)
