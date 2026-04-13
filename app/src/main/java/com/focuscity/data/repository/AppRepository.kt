package com.focuscity.data.repository

import com.focuscity.data.db.AppDatabase
import com.focuscity.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppRepository(private val db: AppDatabase) {

    private val userDao = db.userProfileDao()
    private val sessionDao = db.focusSessionDao()
    private val buildingDao = db.buildingDao()

    // ── User Profile ──────────────────────────────────────────

    val userProfile: Flow<UserProfile> = userDao.getProfile().map { it ?: UserProfile() }

    suspend fun ensureUserProfileExists() {
        if (userDao.getProfileOnce() == null) {
            userDao.insertOrUpdate(UserProfile())
        }
    }

    suspend fun addCoins(amount: Int) = userDao.addCoins(amount)
    suspend fun spendCoins(amount: Int) = userDao.spendCoins(amount)
    suspend fun addFocusMinutes(minutes: Int) = userDao.addFocusMinutes(minutes)
    suspend fun getCoins(): Int = userDao.getProfileOnce()?.coins ?: 0

    // ── Focus Sessions ────────────────────────────────────────

    suspend fun saveSession(session: FocusSession): Long {
        val id = sessionDao.insert(session)
        if (session.coinsEarned > 0) {
            addCoins(session.coinsEarned)
        }
        if (session.actualMinutes > 0) {
            addFocusMinutes(session.actualMinutes)
        }
        return id
    }

    val allSessions: Flow<List<FocusSession>> = sessionDao.getAllSessions()

    suspend fun getSessionsSince(since: Long): List<FocusSession> =
        sessionDao.getSessionsSince(since)

    suspend fun getAverageSessionLength(): Double =
        sessionDao.getAverageSessionLength() ?: 0.0

    suspend fun getTotalSessionCount(): Int =
        sessionDao.getTotalSessionCount()

    suspend fun getFirstSessionDate(): Long? =
        sessionDao.getFirstSessionDate()

    suspend fun getTotalMinutesToday(): Int {
        val startOfDay = getStartOfDay()
        return sessionDao.getTotalMinutesSince(startOfDay) ?: 0
    }

    // ── Buildings ─────────────────────────────────────────────

    val allBuildings: Flow<List<Building>> = buildingDao.getAllBuildings()

    suspend fun placeBuilding(type: BuildingType, gridX: Int, gridY: Int): Long {
        spendCoins(type.cost)
        return buildingDao.insert(
            Building(type = type.name, gridX = gridX, gridY = gridY)
        )
    }

    suspend fun deleteBuilding(building: Building) {
        val type = BuildingType.valueOf(building.type)
        buildingDao.delete(building)
        if (type != BuildingType.HALL) {
            addCoins(type.refund)
        }
    }

    suspend fun moveBuilding(building: Building, newX: Int, newY: Int) {
        buildingDao.update(building.copy(gridX = newX, gridY = newY))
    }

    suspend fun getAllBuildingsOnce(): List<Building> =
        buildingDao.getAllBuildingsOnce()

    // ── Initial Setup ─────────────────────────────────────────

    suspend fun initializeCity() {
        val buildings = buildingDao.getAllBuildingsOnce()
        if (buildings.isEmpty()) {
            // Place Hall at center of 16×16 grid (occupies cells 6-8 in both axes)
            buildingDao.insert(
                Building(type = BuildingType.HALL.name, gridX = 6, gridY = 6)
            )
        }
    }

    // ── Helpers ───────────────────────────────────────────────

    private fun getStartOfDay(): Long {
        val now = System.currentTimeMillis()
        val dayMs = 86_400_000L
        return (now / dayMs) * dayMs
    }
}
