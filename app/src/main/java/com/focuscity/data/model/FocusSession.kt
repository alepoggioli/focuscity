package com.focuscity.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_session")
data class FocusSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,        // "TIMER" or "STOPWATCH"
    val difficulty: String,  // "EASY" or "HARD"
    val targetMinutes: Int? = null,
    val actualMinutes: Int = 0,
    val coinsEarned: Int = 0,
    val violations: Int = 0,
    val maxForgiveness: Int = 0,
    val completed: Boolean = false,
    val cancelledEarly: Boolean = false,
    val timeOutsideAppSeconds: Int = 0,
    val timeOutsideAppInstances: Int = 0,
    val date: Long = System.currentTimeMillis()
)
