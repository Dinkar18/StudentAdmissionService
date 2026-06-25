package com.dp.padhobihar.utils

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RateLimiterTest {

    @Before
    fun setup() {
        RateLimiter.reset()
    }

    @Test
    fun `canProceed returns true for first 3 attempts`() {
        val key = "test@email.com"
        repeat(3) {
            assertTrue("Attempt ${it + 1} should be allowed", RateLimiter.canProceed(key))
            RateLimiter.record(key)
        }
    }

    @Test
    fun `canProceed returns false after 3 attempts`() {
        val key = "blocked@email.com"
        repeat(3) { RateLimiter.record(key) }
        assertFalse("4th attempt should be blocked", RateLimiter.canProceed(key))
    }

    @Test
    fun `remainingSeconds returns positive value after exhaustion`() {
        val key = "exhaust@email.com"
        repeat(3) { RateLimiter.record(key) }
        val remaining = RateLimiter.remainingSeconds(key)
        assertTrue("Should have remaining seconds > 0, got $remaining", remaining > 0)
    }
}
