package com.dp.padhobihar.domain.usecase.auth

import com.dp.padhobihar.domain.model.User
import com.dp.padhobihar.domain.repository.AuthRepository
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): User? = authRepository.getCurrentUser()
    fun isLoggedIn(): Boolean = authRepository.isLoggedIn()
}
