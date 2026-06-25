package com.dp.padhobihar.data.repository

import com.dp.padhobihar.domain.model.College
import com.dp.padhobihar.domain.repository.CollegeRepository
import com.dp.padhobihar.utils.safeCall
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseCollegeRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : CollegeRepository {

    override suspend fun getActiveColleges(): Result<List<College>> = safeCall {
        val docs = firestore.collection("colleges").get().await()
        docs.mapNotNull { it.toObject<College>().copy(id = it.id) }
    }

    override suspend fun addCollege(college: College): Result<Unit> = safeCall {
        firestore.collection("colleges").document(college.id).set(college).await()
    }
}
