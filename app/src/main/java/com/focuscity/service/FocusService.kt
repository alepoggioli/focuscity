package com.focuscity.service

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.focuscity.data.db.AppDatabase
import com.focuscity.data.model.FocusSession
import com.focuscity.data.repository.AppRepository
import com.focuscity.engine.CoinCalculator
import com.focuscity.notification.NotificationHelper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// ── Shared types ────────────────────────────────────────────

enum class SessionType { TIMER, STOPWATCH }
enum class Difficulty { EASY, HARD }
enum class EndReason { NONE, COMPLETED, CANCELLED, FORGIVENESS_EXCEEDED, APP_SWITCH }

data class SessionConfig(
    val type: SessionType = SessionType.TIMER,
    val difficulty: Difficulty = Difficulty.EASY,
    val targetMinutes: Int = 25,
    val maxForgiveness: Int = 3
)

data class SessionState(
    val isRunning: Boolean = false,
    val config: SessionConfig = SessionConfig(),
    val elapsedSeconds: Long = 0,
    val violations: Int = 0,
    val forgivenessRemaining: Int = 3,
    val coinsEarned: Int = 0,
    val isComplete: Boolean = false,
    val endReason: EndReason = EndReason.NONE,
    val finalMinutes: Int = 0,
    val finalCoins: Int = 0,
    val timeOutsideSeconds: Int = 0,
    val timeOutsideInstances: Int = 0
)

// ── Service ─────────────────────────────────────────────────

class FocusService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var timerJob: Job? = null
    private lateinit var repository: AppRepository
    private var lifecycleObserver: DefaultLifecycleObserver? = null
    private var lastBackgroundTimeMs: Long? = null

    companion object {
        private val _sessionState = MutableStateFlow(SessionState())
        val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

        private const val PREFS_NAME = "focus_service_prefs"
        private const val KEY_IS_RUNNING = "is_running"
        private const val KEY_TYPE = "type"
        private const val KEY_DIFFICULTY = "difficulty"
        private const val KEY_TARGET = "target"
        private const val KEY_FORGIVENESS_MAX = "forgiveness_max"
        private const val KEY_ELAPSED = "elapsed"
        private const val KEY_VIOLATIONS = "violations"
        private const val KEY_FORGIVENESS_REM = "forgiveness_rem"
        private const val KEY_TIME_OUT_SEC = "time_out_sec"
        private const val KEY_TIME_OUT_INST = "time_out_inst"

        const val EXTRA_TYPE = "session_type"
        const val EXTRA_DIFFICULTY = "difficulty"
        const val EXTRA_TARGET = "target_minutes"
        const val EXTRA_FORGIVENESS = "max_forgiveness"
        const val ACTION_START = "com.focuscity.ACTION_START"
        const val ACTION_STOP = "com.focuscity.ACTION_STOP"

        fun resetState() {
            _sessionState.value = SessionState()
        }
    }

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getInstance(applicationContext)
        repository = AppRepository(db)
        NotificationHelper.createChannels(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null || intent.action == null) {
            // Service restarted by system
            val saved = loadSessionState()
            if (saved != null && saved.isRunning) {
                resumeSession(saved)
            }
        } else {
            when (intent.action) {
                ACTION_START -> {
                    val config = SessionConfig(
                        type = SessionType.valueOf(intent.getStringExtra(EXTRA_TYPE) ?: "TIMER"),
                        difficulty = Difficulty.valueOf(intent.getStringExtra(EXTRA_DIFFICULTY) ?: "EASY"),
                        targetMinutes = intent.getIntExtra(EXTRA_TARGET, 25),
                        maxForgiveness = intent.getIntExtra(EXTRA_FORGIVENESS, 3)
                    )
                    startSession(config)
                }
                ACTION_STOP -> endSession(EndReason.CANCELLED)
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ── Session lifecycle ─────────────────────────────────

    private fun startSession(config: SessionConfig) {
        _sessionState.value = SessionState(
            isRunning = true,
            config = config,
            forgivenessRemaining = config.maxForgiveness
        )

        // Start foreground with initial notification
        val notification = NotificationHelper.buildFocusNotification(
            this,
            formatTime(0, config),
            config.type == SessionType.TIMER
        )
        startForeground(NotificationHelper.FOCUS_NOTIFICATION_ID, notification)

        // Tick every second
        timerJob = serviceScope.launch {
            var seconds = _sessionState.value.elapsedSeconds
            while (isActive) {
                delay(1000L)
                seconds++

                val state = _sessionState.value
                if (!state.isRunning) break

                val elapsedMinutes = (seconds / 60).toInt()

                // Check time limit
                val maxSeconds: Long = when {
                    config.type == SessionType.TIMER ->
                        config.targetMinutes.toLong() * 60
                    config.difficulty == Difficulty.EASY ->
                        CoinCalculator.MAX_EASY_MINUTES.toLong() * 60
                    else -> Long.MAX_VALUE // hard stopwatch = unlimited
                }

                if (seconds >= maxSeconds) {
                    _sessionState.value = state.copy(elapsedSeconds = seconds)
                    endSession(EndReason.COMPLETED)
                    return@launch
                }

                // Update state
                val currentCoins = CoinCalculator.calculateCoins(elapsedMinutes)
                val afterPenalty = CoinCalculator.applyViolationPenalty(currentCoins, state.violations)

                val newState = state.copy(
                    elapsedSeconds = seconds,
                    coinsEarned = afterPenalty
                )
                _sessionState.value = newState

                // Persist state regularly
                saveSessionState(newState)

                // Update notification every 5 seconds to reduce overhead
                if (seconds % 5 == 0L) {
                    updateNotification(seconds, config, afterPenalty)
                }
            }
        }

        // Monitor app foreground/background
        setupBackgroundMonitoring()
    }

    private fun resumeSession(savedState: SessionState) {
        _sessionState.value = savedState
        
        // Ensure foreground
        val notification = NotificationHelper.buildFocusNotification(
            this,
            formatTime(savedState.elapsedSeconds, savedState.config),
            savedState.config.type == SessionType.TIMER,
            savedState.coinsEarned
        )
        startForeground(NotificationHelper.FOCUS_NOTIFICATION_ID, notification)
        
        startTimerLoop(savedState.config)
        setupBackgroundMonitoring()
    }

    private fun startTimerLoop(config: SessionConfig) {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            var seconds = _sessionState.value.elapsedSeconds
            while (isActive) {
                delay(1000L)
                seconds++

                val state = _sessionState.value
                if (!state.isRunning) break

                val elapsedMinutes = (seconds / 60).toInt()
                val maxSeconds: Long = when {
                    config.type == SessionType.TIMER -> config.targetMinutes.toLong() * 60
                    config.difficulty == Difficulty.EASY -> CoinCalculator.MAX_EASY_MINUTES.toLong() * 60
                    else -> Long.MAX_VALUE
                }

                if (seconds >= maxSeconds) {
                    _sessionState.value = state.copy(elapsedSeconds = seconds)
                    endSession(EndReason.COMPLETED)
                    return@launch
                }

                val currentCoins = CoinCalculator.calculateCoins(elapsedMinutes)
                val afterPenalty = CoinCalculator.applyViolationPenalty(currentCoins, state.violations)

                val newState = state.copy(elapsedSeconds = seconds, coinsEarned = afterPenalty)
                _sessionState.value = newState
                saveSessionState(newState)

                if (seconds % 5 == 0L) {
                    updateNotification(seconds, config, afterPenalty)
                }
            }
        }
    }

    private fun endSession(reason: EndReason) {
        val state = _sessionState.value
        if (!state.isRunning && !state.isComplete) {
            stopCleanup()
            return
        }

        // If we end while backgrounded, update time outside.
        var finalOutSecs = state.timeOutsideSeconds
        lastBackgroundTimeMs?.let { bgTime ->
            val timeOutside = ((System.currentTimeMillis() - bgTime) / 1000).toInt()
            finalOutSecs += timeOutside
            lastBackgroundTimeMs = null
        }

        val elapsedMinutes = (state.elapsedSeconds / 60).toInt()
        val finalCoins = CoinCalculator.calculateFinalCoins(
            minutes = elapsedMinutes,
            violations = state.violations,
            wasForgivenessExceeded = reason == EndReason.FORGIVENESS_EXCEEDED,
            wasCancelled = reason == EndReason.CANCELLED
        )

        _sessionState.value = state.copy(
            isRunning = false,
            isComplete = true,
            endReason = reason,
            finalMinutes = elapsedMinutes,
            finalCoins = finalCoins,
            timeOutsideSeconds = finalOutSecs
        )

        // Persist session
        serviceScope.launch {
            repository.saveSession(
                FocusSession(
                    type = state.config.type.name,
                    difficulty = state.config.difficulty.name,
                    targetMinutes = if (state.config.type == SessionType.TIMER)
                        state.config.targetMinutes else null,
                    actualMinutes = elapsedMinutes,
                    coinsEarned = finalCoins,
                    violations = state.violations,
                    maxForgiveness = state.config.maxForgiveness,
                    completed = reason == EndReason.COMPLETED,
                    cancelledEarly = reason == EndReason.CANCELLED,
                    timeOutsideSeconds = finalOutSecs,
                    timeOutsideInstances = state.timeOutsideInstances
                )
            )
        }

        // Notify user
        if (reason == EndReason.COMPLETED || reason == EndReason.APP_SWITCH) {
            NotificationHelper.showSessionCompleteNotification(this, finalCoins, elapsedMinutes)
        }

        clearSavedSession()
        stopCleanup()
    }

    // ── Hard mode ─────────────────────────────────────────

    // ── Background Monitoring ─────────────────────────────────────────

    private fun setupBackgroundMonitoring() {
        lifecycleObserver = object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                // App left foreground
                val state = _sessionState.value
                if (!state.isRunning) return

                lastBackgroundTimeMs = System.currentTimeMillis()

                if (state.config.difficulty == Difficulty.HARD) {
                    when (state.config.type) {
                        SessionType.STOPWATCH -> {
                            // Stopwatch: session ends immediately
                            endSession(EndReason.APP_SWITCH)
                        }
                        SessionType.TIMER -> {
                            // Timer: count violation
                            val newViolations = state.violations + 1
                            val newForgiveness = state.forgivenessRemaining - 1

                            if (newForgiveness < 0) {
                                endSession(EndReason.FORGIVENESS_EXCEEDED)
                            } else {
                                _sessionState.value = state.copy(
                                    violations = newViolations,
                                    forgivenessRemaining = newForgiveness,
                                    timeOutsideInstances = state.timeOutsideInstances + 1
                                )
                            }
                        }
                    }
                } else {
                    // Easy mode explicitly allows moving out of app
                    _sessionState.value = state.copy(
                        timeOutsideInstances = state.timeOutsideInstances + 1
                    )
                }
            }

            override fun onStart(owner: LifecycleOwner) {
                // App entered foreground
                val state = _sessionState.value
                if (!state.isRunning) return

                lastBackgroundTimeMs?.let { bgTime ->
                    val timeOutside = ((System.currentTimeMillis() - bgTime) / 1000).toInt()
                    _sessionState.value = state.copy(
                        timeOutsideSeconds = state.timeOutsideSeconds + timeOutside
                    )
                    lastBackgroundTimeMs = null
                }
            }
        }

        Handler(Looper.getMainLooper()).post {
            lifecycleObserver?.let {
                ProcessLifecycleOwner.get().lifecycle.addObserver(it)
            }
        }
    }

    // ── Persistence ─────────────────────────────────────────────

    private fun saveSessionState(state: SessionState) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean(KEY_IS_RUNNING, state.isRunning)
            putString(KEY_TYPE, state.config.type.name)
            putString(KEY_DIFFICULTY, state.config.difficulty.name)
            putInt(KEY_TARGET, state.config.targetMinutes)
            putInt(KEY_FORGIVENESS_MAX, state.config.maxForgiveness)
            putLong(KEY_ELAPSED, state.elapsedSeconds)
            putInt(KEY_VIOLATIONS, state.violations)
            putInt(KEY_FORGIVENESS_REM, state.forgivenessRemaining)
            putInt(KEY_TIME_OUT_SEC, state.timeOutsideSeconds)
            putInt(KEY_TIME_OUT_INST, state.timeOutsideInstances)
            apply()
        }
    }

    private fun loadSessionState(): SessionState? {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.contains(KEY_IS_RUNNING)) return null
        
        return SessionState(
            isRunning = prefs.getBoolean(KEY_IS_RUNNING, false),
            config = SessionConfig(
                type = SessionType.valueOf(prefs.getString(KEY_TYPE, "TIMER")!!),
                difficulty = Difficulty.valueOf(prefs.getString(KEY_DIFFICULTY, "EASY")!!),
                targetMinutes = prefs.getInt(KEY_TARGET, 25),
                maxForgiveness = prefs.getInt(KEY_FORGIVENESS_MAX, 3)
            ),
            elapsedSeconds = prefs.getLong(KEY_ELAPSED, 0),
            violations = prefs.getInt(KEY_VIOLATIONS, 0),
            forgivenessRemaining = prefs.getInt(KEY_FORGIVENESS_REM, 3),
            timeOutsideSeconds = prefs.getInt(KEY_TIME_OUT_SEC, 0),
            timeOutsideInstances = prefs.getInt(KEY_TIME_OUT_INST, 0)
        )
    }

    private fun clearSavedSession() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // App swiped away. If a session is running, make sure we save it.
        if (_sessionState.value.isRunning) {
            saveSessionState(_sessionState.value)
        }
        super.onTaskRemoved(rootIntent)
    }

    // ── Helpers ─────────────────────────────────────────

    private fun formatTime(seconds: Long, config: SessionConfig): String {
        return if (config.type == SessionType.TIMER) {
            val remaining = (config.targetMinutes * 60L) - seconds
            val m = (remaining / 60).coerceAtLeast(0)
            val s = (remaining % 60).coerceAtLeast(0)
            String.format("%02d:%02d", m, s)
        } else {
            val m = seconds / 60
            val s = seconds % 60
            String.format("%02d:%02d", m, s)
        }
    }

    private fun updateNotification(seconds: Long, config: SessionConfig, coins: Int) {
        val timeText = formatTime(seconds, config)
        val notification = NotificationHelper.buildFocusNotification(
            this, timeText, config.type == SessionType.TIMER, coins
        )
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NotificationHelper.FOCUS_NOTIFICATION_ID, notification)
    }

    private fun stopCleanup() {
        timerJob?.cancel()
        lifecycleObserver?.let { observer ->
            Handler(Looper.getMainLooper()).post {
                ProcessLifecycleOwner.get().lifecycle.removeObserver(observer)
            }
        }
        lifecycleObserver = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        timerJob?.cancel()
        serviceScope.cancel()
        lifecycleObserver?.let { observer ->
            Handler(Looper.getMainLooper()).post {
                ProcessLifecycleOwner.get().lifecycle.removeObserver(observer)
            }
        }
        super.onDestroy()
    }
}
