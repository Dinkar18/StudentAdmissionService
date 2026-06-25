package com.dp.padhobihar.domain.model

data class User(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val role: String = "STUDENT",
    val referralCode: String = "",
    val parentId: String = "",
    val district: String = "",
    val status: String = "ACTIVE",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getUserRole(): Role = try {
        Role.valueOf(role)
    } catch (e: Exception) {
        Role.STUDENT
    }

    fun isActive(): Boolean = status == "ACTIVE"
}
