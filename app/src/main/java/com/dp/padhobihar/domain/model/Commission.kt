package com.dp.padhobihar.domain.model

data class Commission(
    val id: String = "",
    val studentId: String = "",
    val agentId: String = "",
    val collegeId: String = "",
    val totalAmount: Long = 0,
    val adminShare: Long = 0,
    val agentShare: Long = 0,
    val status: PaymentStatus = PaymentStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val paidAt: Long? = null
)
