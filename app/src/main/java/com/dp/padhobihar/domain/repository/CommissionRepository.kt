package com.dp.padhobihar.domain.repository

import com.dp.padhobihar.domain.model.Commission

interface CommissionRepository {
    suspend fun getByAgent(agentId: String): Result<List<Commission>>
    suspend fun getAll(): Result<List<Commission>>
    suspend fun create(commission: Map<String, Any>): Result<Unit>
    suspend fun markPaid(commissionId: String): Result<Unit>
}
