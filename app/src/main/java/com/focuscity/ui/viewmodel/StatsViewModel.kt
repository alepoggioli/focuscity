package com.focuscity.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focuscity.data.db.AppDatabase
import com.focuscity.data.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class StatsState(
    val avgSessionMinutes: Double = 0.0,
    val avgSessionsPerWeek: Double = 0.0,
    val totalSessions: Int = 0,
    val totalMinutes: Int = 0,
    val selectedRange: Int = 7,             // days
    val focusDays: List<Boolean> = emptyList(),
    val totalActiveDays: Int = 0,
    val avgTimeOutsideSeconds: Int = 0,
    val avgTimeOutsideInstances: Int = 0,
    val isLoading: Boolean = true
)

class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository
    private val _statsState = MutableStateFlow(StatsState())
    val statsState: StateFlow<StatsState> = _statsState.asStateFlow()

    init {
        val db = AppDatabase.getInstance(application)
        repository = AppRepository(db)
        loadStats()
    }

    fun setRange(days: Int) {
        _statsState.value = _statsState.value.copy(selectedRange = days)
        loadFocusDays(days)
    }

    private fun loadStats() {
        viewModelScope.launch {
            val avgLength = repository.getAverageSessionLength()
            val totalCount = repository.getTotalSessionCount()
            val profile = repository.userProfile.first()

            // Sessions per week
            val firstDate = repository.getFirstSessionDate()
            val weeksActive = if (firstDate != null) {
                val daysSinceFirst = ((System.currentTimeMillis() - firstDate) / 86_400_000.0)
                    .coerceAtLeast(1.0)
                daysSinceFirst / 7.0
            } else 1.0

            val sessionsPerWeek = if (totalCount > 0) totalCount / weeksActive else 0.0

            val totalActiveDays = if (firstDate != null) {
                ((System.currentTimeMillis() - firstDate) / 86_400_000.0).toInt().coerceAtLeast(1)
            } else 0

            // Advanced stats (average over easy/hard sessions)
            val allSessions = repository.allSessions.first()
            val outsideSecondsTotal = allSessions.sumOf { it.timeOutsideSeconds }
            val outsideInstancesTotal = allSessions.sumOf { it.timeOutsideInstances }
            
            val avgOutsideSecs = if (totalCount > 0) outsideSecondsTotal / totalCount else 0
            val avgOutsideInst = if (totalCount > 0) outsideInstancesTotal / totalCount else 0

            _statsState.value = _statsState.value.copy(
                avgSessionMinutes = avgLength,
                avgSessionsPerWeek = sessionsPerWeek,
                totalSessions = totalCount,
                totalMinutes = profile.totalFocusMinutes,
                totalActiveDays = totalActiveDays,
                avgTimeOutsideSeconds = avgOutsideSecs,
                avgTimeOutsideInstances = avgOutsideInst,
                isLoading = false
            )

            loadFocusDays(_statsState.value.selectedRange)
        }
    }

    private fun loadFocusDays(days: Int) {
        viewModelScope.launch {
            val dayMs = 86_400_000L
            val now = System.currentTimeMillis()
            val since = now - (days.toLong() * dayMs)

            val sessions = repository.getSessionsSince(since)

            // Determine which days had at least one session
            val sessionDays = sessions.map { it.date / dayMs }.toSet()

            val focusDays = (0 until days).map { i ->
                val dayIndex = (now / dayMs) - i
                sessionDays.contains(dayIndex)
            }.reversed()

            _statsState.value = _statsState.value.copy(
                focusDays = focusDays,
                selectedRange = days
            )
        }
    }

    fun refresh() {
        _statsState.value = _statsState.value.copy(isLoading = true)
        loadStats()
    }
}
