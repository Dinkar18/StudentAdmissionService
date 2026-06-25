package com.dp.padhobihar.utils

import java.util.concurrent.ConcurrentHashMap

object RateLimiter {
    private val attempts = ConcurrentHashMap<String, MutableList<Long>>()
    private const val MAX_ATTEMPTS = 5
    private const val WINDOW_MS = 120_000L // 2 minutes

    fun canProceed(key: String): Boolean {
        val now = System.currentTimeMillis()
        val timestamps = attempts.getOrPut(key) { mutableListOf() }
        timestamps.removeAll { now - it > WINDOW_MS }
        return timestamps.size < MAX_ATTEMPTS
    }

    fun record(key: String) {
        val now = System.currentTimeMillis()
        attempts.getOrPut(key) { mutableListOf() }.add(now)
    }

    fun remainingSeconds(key: String): Int {
        val timestamps = attempts[key] ?: return 0
        if (timestamps.isEmpty()) return 0
        val oldest = timestamps.first()
        val remaining = (WINDOW_MS - (System.currentTimeMillis() - oldest)) / 1000
        return remaining.toInt().coerceAtLeast(0)
    }

    // For testing
    internal fun reset() {
        attempts.clear()
    }
}
