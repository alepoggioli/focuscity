package com.focuscity.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val coins: Int = 0,
    val totalFocusMinutes: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
