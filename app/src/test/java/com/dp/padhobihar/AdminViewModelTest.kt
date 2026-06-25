package com.dp.padhobihar

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests the referral code generation logic used in AdminViewModel.addAgent:
 * referralCode = name.take(4).uppercase() + phone.takeLast(4)
 */
class AdminViewModelTest {

    private fun generateReferralCode(name: String, phone: String): String {
        return name.take(4).uppercase() + phone.takeLast(4)
    }

    @Test
    fun `referral code is first 4 chars of name uppercase plus last 4 of phone`() {
        val code = generateReferralCode("Rahul Kumar", "9876543210")
        assertEquals("RAHU3210", code)
    }

    @Test
    fun `short name uses all available chars`() {
        val code = generateReferralCode("Om", "9876543210")
        assertEquals("OM3210", code)
    }

    @Test
    fun `name with lowercase is uppercased`() {
        val code = generateReferralCode("priya", "1234567890")
        assertEquals("PRIY7890", code)
    }

    @Test
    fun `phone with exactly 4 digits uses all`() {
        val code = generateReferralCode("TestAgent", "1234")
        assertEquals("TEST1234", code)
    }
}
