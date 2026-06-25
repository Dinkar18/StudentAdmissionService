package com.dp.padhobihar

import com.dp.padhobihar.data.local.CollegeDataSource
import com.dp.padhobihar.data.local.LocalCollege
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CollegeDataSourceTest {

    @Before
    fun setup() {
        val field = CollegeDataSource::class.java.getDeclaredField("colleges")
        field.isAccessible = true
        field.set(CollegeDataSource, listOf(
            LocalCollege("1", "Indian Institute of Technology Delhi", "Delhi", "New Delhi"),
            LocalCollege("2", "Indian Institute of Technology Bombay", "Maharashtra", "Mumbai"),
            LocalCollege("3", "Bihar Engineering College", "Bihar", "Patna"),
            LocalCollege("4", "National Institute of Technology Patna", "Bihar", "Patna")
        ))
    }

    @Test
    fun `search iit returns results containing indian institute of technology`() {
        val results = CollegeDataSource.search(null, "iit")
        assertTrue("Expected IIT results", results.isNotEmpty())
        assertTrue(results.all { it.name.lowercase().contains("indian institute of technology") })
    }

    @Test
    fun `search with less than 3 chars returns empty`() {
        val results = CollegeDataSource.search(null, "ii")
        assertTrue(results.isEmpty())
    }

    @Test
    fun `search nit returns national institute of technology`() {
        val results = CollegeDataSource.search(null, "nit")
        assertTrue("Expected NIT results", results.isNotEmpty())
        assertTrue(results.any { it.name.lowercase().contains("national institute of technology") })
    }
}
