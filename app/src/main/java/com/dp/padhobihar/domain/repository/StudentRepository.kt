package com.dp.padhobihar.domain.repository

import com.dp.padhobihar.domain.model.ApplicationStatus
import com.dp.padhobihar.domain.model.Student

interface StudentRepository {
    suspend fun getStudentByUserId(userId: String): Result<Student?>
    suspend fun getStudentsByAgent(agentId: String): Result<List<Student>>
    suspend fun getAllStudents(): Result<List<Student>>
    suspend fun updateStatus(studentId: String, status: ApplicationStatus): Result<Unit>
    suspend fun updateFields(studentId: String, fields: Map<String, Any>): Result<Unit>
}
