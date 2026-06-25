package com.dp.padhobihar.data.repository

import com.dp.padhobihar.domain.model.Commission
import com.dp.padhobihar.domain.model.PaymentStatus
import com.dp.padhobihar.domain.repository.CommissionRepository
import com.dp.padhobihar.utils.safeCall
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseCommissionRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : CommissionRepository {

    private val collection = firestore.collection("commissions")

    override suspend fun getByAgent(agentId: String): Result<List<Commission>> = safeCall {
        val docs = collection.whereEqualTo("agentId", agentId).get().await()
        docs.map { parseCommission(it.id, it.data) }
    }

    override suspend fun getAll(): Result<List<Commission>> = safeCall {
        val docs = collection.get().await()
        docs.map { parseCommission(it.id, it.data) }
    }

    override suspend fun create(commission: Map<String, Any>): Result<Unit> = safeCall {
        collection.add(commission).await()
        Unit
    }

    override suspend fun markPaid(commissionId: String): Result<Unit> = safeCall {
        collection.document(commissionId).update(
            mapOf("status" to "PAID", "paidAt" to System.currentTimeMillis())
        ).await()
    }

    private fun parseCommission(id: String, data: Map<String, Any>): Commission {
        return Commission(
            id = id,
            studentId = data["studentId"] as? String ?: "",
            agentId = data["agentId"] as? String ?: "",
            collegeId = data["collegeId"] as? String ?: "",
            totalAmount = (data["totalAmount"] as? Number)?.toLong() ?: 0,
            adminShare = (data["adminShare"] as? Number)?.toLong() ?: 0,
            agentShare = (data["agentShare"] as? Number)?.toLong() ?: 0,
            status = try { PaymentStatus.valueOf(data["status"] as? String ?: "PENDING") }
                     catch (e: Exception) { PaymentStatus.PENDING },
            createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0,
            paidAt = (data["paidAt"] as? Number)?.toLong()
        )
    }
}
