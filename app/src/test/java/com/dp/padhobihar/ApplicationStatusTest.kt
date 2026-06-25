package com.dp.padhobihar

import com.dp.padhobihar.domain.model.ApplicationStatus
import org.junit.Assert.*
import org.junit.Test

class ApplicationStatusTest {

    @Test
    fun `enum has exactly 9 values`() {
        assertEquals(9, ApplicationStatus.values().size)
    }

    @Test
    fun `valueOf works for all statuses`() {
        val expected = listOf(
            "REGISTERED", "PROFILE_COMPLETE", "DOCS_UPLOADED",
            "COLLEGE_REQUESTED", "COLLEGE_SUGGESTED", "COLLEGE_CONFIRMED",
            "ADMITTED", "WITHDRAWN", "REJECTED"
        )
        expected.forEach { name ->
            assertEquals(name, ApplicationStatus.valueOf(name).name)
        }
    }
}
