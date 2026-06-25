package com.dp.padhobihar.domain.repository

import com.dp.padhobihar.domain.model.User

interface UserRepository {
    suspend fun getUser(userId: String): Result<User?>
    suspend fun createUser(user: User): Result<Unit>
    suspend fun updateUser(user: User): Result<Unit>
    suspend fun getUsersByRole(role: String): Result<List<User>>
}
