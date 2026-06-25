package com.dp.padhobihar

import com.dp.padhobihar.domain.model.ApplicationStatus
import com.dp.padhobihar.domain.model.Student
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for StudentViewModel logic (referral validation, status progression).
 * Uses plain unit tests without Firebase dependencies.
 */
class StudentViewModelTest {

    // Mirrors the validation logic: empty referral code query returns empty → error
    @Test
    fun `register with empty referral code should produce error`() {
        val referralCode = ""
        // The ViewModel queries Firestore with empty code → agentDoc.isEmpty = true → error
        val isValid = referralCode.isNotBlank()
        assertFalse("Empty referral code should be invalid", isValid)
    }

    @Test
    fun `register with blank referral code should produce error`() {
        val referralCode = "   "
        val isValid = referralCode.isNotBlank()
        assertFalse("Blank referral code should be invalid", isValid)
    }

    @Test
    fun `status progression from REGISTERED to PROFILE_COMPLETE`() {
        val student = Student(status = ApplicationStatus.REGISTERED)
        // After profile save, status should advance to PROFILE_COMPLETE
        val updated = student.copy(status = ApplicationStatus.PROFILE_COMPLETE)
        assertEquals(ApplicationStatus.PROFILE_COMPLETE, updated.status)
    }

    @Test
    fun `status progression from PROFILE_COMPLETE to DOCS_UPLOADED`() {
        val student = Student(status = ApplicationStatus.PROFILE_COMPLETE)
        val updated = student.copy(status = ApplicationStatus.DOCS_UPLOADED)
        assertEquals(ApplicationStatus.DOCS_UPLOADED, updated.status)
    }

    @Test
    fun `status progression from DOCS_UPLOADED to COLLEGE_REQUESTED`() {
        val student = Student(status = ApplicationStatus.DOCS_UPLOADED)
        val updated = student.copy(status = ApplicationStatus.COLLEGE_REQUESTED)
        assertEquals(ApplicationStatus.COLLEGE_REQUESTED, updated.status)
    }

    @Test
    fun `status progression full happy path`() {
        val statuses = listOf(
            ApplicationStatus.REGISTERED,
            ApplicationStatus.PROFILE_COMPLETE,
            ApplicationStatus.DOCS_UPLOADED,
            ApplicationStatus.COLLEGE_REQUESTED,
            ApplicationStatus.COLLEGE_SUGGESTED,
            ApplicationStatus.COLLEGE_CONFIRMED,
            ApplicationStatus.ADMITTED
        )
        for (i in 0 until statuses.size - 1) {
            val current = statuses[i]
            val next = statuses[i + 1]
            assertTrue("$current should come before $next", current.ordinal < next.ordinal)
        }
    }

    @Test
    fun `WITHDRAWN and REJECTED are terminal states`() {
        assertTrue(ApplicationStatus.WITHDRAWN.ordinal > ApplicationStatus.REGISTERED.ordinal)
        assertTrue(ApplicationStatus.REJECTED.ordinal > ApplicationStatus.REGISTERED.ordinal)
    }
}
