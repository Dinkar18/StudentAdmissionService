package com.dp.padhobihar.data.repository

import com.dp.padhobihar.domain.model.ApplicationStatus
import com.dp.padhobihar.domain.model.Student
import com.dp.padhobihar.domain.repository.StudentRepository
import com.dp.padhobihar.utils.safeCall
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseStudentRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : StudentRepository {

    private val collection = firestore.collection("students")

    override suspend fun getStudentByUserId(userId: String): Result<Student?> = safeCall {
        val docs = collection.whereEqualTo("userId", userId).limit(1).get().await()
        docs.documents.firstOrNull()?.let { parseStudent(it.id, it.data ?: emptyMap()) }
    }

    override suspend fun getStudentsByAgent(agentId: String): Result<List<Student>> = safeCall {
        val docs = collection.whereEqualTo("agentId", agentId).get().await()
        docs.map { parseStudent(it.id, it.data ?: emptyMap()) }
    }

    override suspend fun getAllStudents(): Result<List<Student>> = safeCall {
        val docs = collection.get().await()
        docs.map { parseStudent(it.id, it.data ?: emptyMap()) }
    }

    override suspend fun updateStatus(studentId: String, status: ApplicationStatus): Result<Unit> = safeCall {
        collection.document(studentId).update("status", status.name).await()
    }

    override suspend fun updateFields(studentId: String, fields: Map<String, Any>): Result<Unit> = safeCall {
        collection.document(studentId).update(fields).await()
    }

    private fun parseStudent(id: String, data: Map<String, Any>): Student {
        return Student(
            id = id,
            userId = data["userId"] as? String ?: "",
            name = data["name"] as? String ?: "",
            phone = data["phone"] as? String ?: "",
            fatherName = data["fatherName"] as? String ?: "",
            village = data["village"] as? String ?: "",
            district = data["district"] as? String ?: "",
            marks = (data["marks"] as? Number)?.toFloat() ?: 0f,
            courseInterest = data["courseInterest"] as? String ?: "",
            agentId = data["agentId"] as? String ?: "",
            agentCode = data["agentCode"] as? String ?: "",
            requestedCollegeId = data["requestedCollegeId"] as? String ?: "",
            suggestedCollegeId = data["suggestedCollegeId"] as? String ?: "",
            confirmedCollegeId = data["confirmedCollegeId"] as? String ?: "",
            status = try {
                ApplicationStatus.valueOf(data["status"] as? String ?: "REGISTERED")
            } catch (e: Exception) {
                ApplicationStatus.REGISTERED
            }
        )
    }
}
