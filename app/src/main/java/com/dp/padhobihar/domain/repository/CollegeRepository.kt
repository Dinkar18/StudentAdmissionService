package com.dp.padhobihar.domain.repository

import com.dp.padhobihar.domain.model.College

interface CollegeRepository {
    suspend fun getActiveColleges(): Result<List<College>>
    suspend fun addCollege(college: College): Result<Unit>
}
