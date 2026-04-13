package com.focuscity.engine

/**
 * Central coin calculation logic for focus sessions.
 *
 * Rules:
 * - 3 coins per 5 minutes of focus (0.6 coins/min, no fractions, floor)
 * - Minimum 10 minutes for any coins
 * - Hard mode timer: -10% per violation, stacking
 * - Forgiveness exceeded: only 3 coins (1 block)
 * - Cancel at ≥60 min: 50% of earned coins; <60 min: 0 coins
 */
object CoinCalculator {

    private const val COINS_PER_BLOCK = 3
    private const val BLOCK_MINUTES = 5
    private const val VIOLATION_PENALTY = 0.10
    private const val CANCEL_THRESHOLD_MINUTES = 60
    private const val CANCEL_REFUND_RATE = 0.50
    private const val FORGIVENESS_EXCEEDED_COINS = 3

    const val MIN_SESSION_MINUTES = 10
    const val MAX_EASY_MINUTES = 180
    const val MAX_FORGIVENESS = 5
    const val TIMER_STEP = 5

    /** Base coins for a given number of focused minutes. Returns 0 if below minimum. */
    fun calculateCoins(minutes: Int): Int {
        if (minutes < MIN_SESSION_MINUTES) return 0
        return (minutes / BLOCK_MINUTES) * COINS_PER_BLOCK
    }

    /** Apply -10% per violation to base coins. */
    fun applyViolationPenalty(baseCoins: Int, violations: Int): Int {
        if (violations <= 0) return baseCoins
        val multiplier = (1.0 - VIOLATION_PENALTY * violations).coerceAtLeast(0.0)
        return (baseCoins * multiplier).toInt()
    }

    /** Apply cancel penalty: ≥60 min → 50%, <60 min → 0. */
    fun applyCancelPenalty(earnedCoins: Int, minutesFocused: Int): Int {
        return if (minutesFocused >= CANCEL_THRESHOLD_MINUTES) {
            (earnedCoins * CANCEL_REFUND_RATE).toInt()
        } else {
            0
        }
    }

    /** Coins when forgiveness limit is exceeded: fixed 3 coins. */
    fun forgivenessExceededCoins(): Int = FORGIVENESS_EXCEEDED_COINS

    /**
     * Calculate the final coin reward for a completed/ended session.
     *
     * @param minutes     Total minutes focused
     * @param violations  Number of app-switch violations (hard mode timer)
     * @param wasForgivenessExceeded  True if the session ended due to exceeding forgiveness
     * @param wasCancelled True if the user manually cancelled
     */
    fun calculateFinalCoins(
        minutes: Int,
        violations: Int = 0,
        wasForgivenessExceeded: Boolean = false,
        wasCancelled: Boolean = false
    ): Int {
        if (wasForgivenessExceeded) return forgivenessExceededCoins()

        val baseCoins = calculateCoins(minutes)
        val afterPenalty = applyViolationPenalty(baseCoins, violations)

        return if (wasCancelled) {
            applyCancelPenalty(afterPenalty, minutes)
        } else {
            afterPenalty
        }
    }

    /** Round a user-typed value up to the nearest 5-minute block, clamped to [10, max]. */
    fun roundUpToBlock(value: Int, max: Int = MAX_EASY_MINUTES): Int {
        val rounded = if (value % TIMER_STEP == 0) value
                      else ((value / TIMER_STEP) + 1) * TIMER_STEP
        return rounded.coerceIn(MIN_SESSION_MINUTES, max)
    }
}
