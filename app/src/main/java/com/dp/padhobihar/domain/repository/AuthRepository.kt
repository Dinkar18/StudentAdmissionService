package com.dp.padhobihar.domain.repository

import android.app.Activity
import com.dp.padhobihar.domain.model.User

interface AuthRepository {
    suspend fun sendOtp(phone: String, activity: Activity): Result<String>
    suspend fun verifyOtp(verificationId: String, otp: String): Result<User?>
    suspend fun loginWithEmail(email: String, password: String): Result<User?>
    suspend fun registerWithEmail(email: String, password: String): Result<String>
    suspend fun sendEmailVerification(): Result<Unit>
    fun isEmailVerified(): Boolean
    suspend fun getCurrentUser(): User?
    fun isLoggedIn(): Boolean
    fun getCurrentUid(): String?
    fun logout()
}
