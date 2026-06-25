package com.dp.padhobihar.utils

import com.dp.padhobihar.domain.model.Role
import org.junit.Assert.*
import org.junit.Test

/**
 * Navigator requires Android Context and Intent, so we cannot test navigation directly.
 * We verify the Role enum mapping completeness that Navigator.toHome relies on.
 */
class NavigatorTest {

    @Test
    fun `Role enum has exactly 3 values for Navigator when-mapping`() {
        assertEquals(3, Role.values().size)
        assertNotNull(Role.valueOf("ADMIN"))
        assertNotNull(Role.valueOf("AGENT"))
        assertNotNull(Role.valueOf("STUDENT"))
    }
}
