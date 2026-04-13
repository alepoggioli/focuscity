package com.focuscity.data.db

import androidx.room.*
import com.focuscity.data.model.FocusSession
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {

    @Insert
    suspend fun insert(session: FocusSession): Long

    @Query("SELECT * FROM focus_session ORDER BY date DESC")
    fun getAllSessions(): Flow<List<FocusSession>>

    @Query("SELECT * FROM focus_session WHERE date >= :since ORDER BY date DESC")
    suspend fun getSessionsSince(since: Long): List<FocusSession>

    @Query("SELECT AVG(actualMinutes) FROM focus_session WHERE actualMinutes > 0")
    suspend fun getAverageSessionLength(): Double?

    @Query("SELECT COUNT(*) FROM focus_session")
    suspend fun getTotalSessionCount(): Int

    @Query("SELECT SUM(coinsEarned) FROM focus_session")
    suspend fun getTotalCoinsEarned(): Int?

    @Query("SELECT MIN(date) FROM focus_session")
    suspend fun getFirstSessionDate(): Long?

    @Query("SELECT SUM(actualMinutes) FROM focus_session WHERE date >= :since")
    suspend fun getTotalMinutesSince(since: Long): Int?
}
