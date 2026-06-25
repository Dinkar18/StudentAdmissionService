package com.dp.padhobihar.utils

import org.junit.Assert.*
import org.junit.Test

class ValidatorTest {

    // isValidEmail uses android.util.Patterns which is unavailable in unit tests.
    // We test the logic boundary: blank input must be invalid.
    @Test
    fun `isValidEmail rejects blank input`() {
        assertFalse(Validator.isValidEmail(""))
        assertFalse(Validator.isValidEmail("   "))
    }

    // Phone tests
    @Test
    fun `isValidPhone accepts 10 digit number`() {
        assertTrue(Validator.isValidPhone("9876543210"))
    }

    @Test
    fun `isValidPhone rejects less than 10 digits`() {
        assertFalse(Validator.isValidPhone("987654321"))
    }

    @Test
    fun `isValidPhone rejects more than 10 digits`() {
        assertFalse(Validator.isValidPhone("98765432100"))
    }

    @Test
    fun `isValidPhone rejects non-digit characters`() {
        assertFalse(Validator.isValidPhone("98765abcde"))
        assertFalse(Validator.isValidPhone("987 654 32"))
    }

    // Password tests
    @Test
    fun `isValidPassword accepts 6 or more characters`() {
        assertTrue(Validator.isValidPassword("123456"))
        assertTrue(Validator.isValidPassword("abcdefghij"))
    }

    @Test
    fun `isValidPassword rejects less than 6 characters`() {
        assertFalse(Validator.isValidPassword("12345"))
        assertFalse(Validator.isValidPassword(""))
    }

    // Referral code tests
    @Test
    fun `isValidReferralCode accepts 4 to 10 alphanumeric chars`() {
        assertTrue(Validator.isValidReferralCode("ABCD"))
        assertTrue(Validator.isValidReferralCode("RAHU3210"))
        assertTrue(Validator.isValidReferralCode("A1B2C3D4E5"))
    }

    @Test
    fun `isValidReferralCode rejects too short`() {
        assertFalse(Validator.isValidReferralCode("ABC"))
    }

    @Test
    fun `isValidReferralCode rejects too long`() {
        assertFalse(Validator.isValidReferralCode("ABCDEFGHIJK"))
    }

    @Test
    fun `isValidReferralCode rejects special characters`() {
        assertFalse(Validator.isValidReferralCode("AB@D"))
        assertFalse(Validator.isValidReferralCode("AB CD"))
    }

    @Test
    fun `isValidReferralCode rejects blank`() {
        assertFalse(Validator.isValidReferralCode(""))
        assertFalse(Validator.isValidReferralCode("    "))
    }

    // Sanitize tests
    @Test
    fun `sanitize removes angle brackets`() {
        assertEquals("script", Validator.sanitize("<script>"))
    }

    @Test
    fun `sanitize removes quotes`() {
        assertEquals("hello", Validator.sanitize("\"hello\""))
        assertEquals("hello", Validator.sanitize("'hello'"))
    }

    @Test
    fun `sanitize trims whitespace`() {
        assertEquals("hello", Validator.sanitize("  hello  "))
    }

    @Test
    fun `sanitize removes all dangerous chars combined`() {
        assertEquals("scriptalert(XSS)/script", Validator.sanitize("  <script>alert('XSS')</script>  "))
    }
}
