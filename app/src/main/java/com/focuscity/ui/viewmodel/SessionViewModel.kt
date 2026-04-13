package com.focuscity.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import com.focuscity.engine.CoinCalculator
import com.focuscity.service.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SetupState(
    val type: SessionType = SessionType.TIMER,
    val difficulty: Difficulty = Difficulty.EASY,
    val targetMinutes: Int = 25,
    val maxForgiveness: Int = 3,
    val durationText: String = "25"
)

class SessionViewModel(application: Application) : AndroidViewModel(application) {

    private val _setupState = MutableStateFlow(SetupState())
    val setupState: StateFlow<SetupState> = _setupState.asStateFlow()

    /** Live session state from the foreground service */
    val sessionState: StateFlow<SessionState> = FocusService.sessionState

    // ── Setup controls ────────────────────────────────

    fun setType(type: SessionType) {
        _setupState.value = _setupState.value.copy(type = type)
    }

    fun setDifficulty(difficulty: Difficulty) {
        _setupState.value = _setupState.value.copy(difficulty = difficulty)
    }

    fun setTargetMinutes(minutes: Int) {
        val max = CoinCalculator.MAX_EASY_MINUTES
        val clamped = minutes.coerceIn(CoinCalculator.MIN_SESSION_MINUTES, max)
        _setupState.value = _setupState.value.copy(
            targetMinutes = clamped,
            durationText = clamped.toString()
        )
    }

    fun setDurationText(text: String) {
        _setupState.value = _setupState.value.copy(durationText = text)
        // Parse and round up on commit (called when focus leaves the text field)
        val parsed = text.toIntOrNull()
        if (parsed != null) {
            val rounded = CoinCalculator.roundUpToBlock(parsed)
            _setupState.value = _setupState.value.copy(
                targetMinutes = rounded,
                durationText = rounded.toString()
            )
        }
    }

    fun commitDurationText() {
        val parsed = _setupState.value.durationText.toIntOrNull()
        if (parsed != null) {
            val rounded = CoinCalculator.roundUpToBlock(parsed)
            _setupState.value = _setupState.value.copy(
                targetMinutes = rounded,
                durationText = rounded.toString()
            )
        } else {
            // Reset to current target if invalid
            _setupState.value = _setupState.value.copy(
                durationText = _setupState.value.targetMinutes.toString()
            )
        }
    }

    fun setMaxForgiveness(count: Int) {
        _setupState.value = _setupState.value.copy(
            maxForgiveness = count.coerceIn(0, CoinCalculator.MAX_FORGIVENESS)
        )
    }

    // ── Session controls ──────────────────────────────

    fun startSession() {
        val setup = _setupState.value
        val context = getApplication<Application>()

        val intent = Intent(context, FocusService::class.java).apply {
            action = FocusService.ACTION_START
            putExtra(FocusService.EXTRA_TYPE, setup.type.name)
            putExtra(FocusService.EXTRA_DIFFICULTY, setup.difficulty.name)
            putExtra(FocusService.EXTRA_TARGET, setup.targetMinutes)
            putExtra(FocusService.EXTRA_FORGIVENESS, setup.maxForgiveness)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopSession() {
        val context = getApplication<Application>()
        val intent = Intent(context, FocusService::class.java).apply {
            action = FocusService.ACTION_STOP
        }
        context.startService(intent)
    }

    fun resetSession() {
        FocusService.resetState()
    }

    // ── Computed properties ───────────────────────────

    fun estimatedCoins(): Int {
        val setup = _setupState.value
        return CoinCalculator.calculateCoins(setup.targetMinutes)
    }
}
