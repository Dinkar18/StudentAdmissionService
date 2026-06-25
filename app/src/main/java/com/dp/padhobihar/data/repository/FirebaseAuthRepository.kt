package com.dp.padhobihar.data.repository

import android.app.Activity
import com.dp.padhobihar.domain.model.User
import com.dp.padhobihar.domain.repository.AuthRepository
import com.dp.padhobihar.domain.repository.UserRepository
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.coroutines.resume

class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository
) : AuthRepository {

    override suspend fun sendOtp(phone: String, activity: Activity): Result<String> {
        return suspendCancellableCoroutine { continuation ->
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber("+91$phone")
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) = Unit

                    override fun onVerificationFailed(e: FirebaseException) {
                        if (continuation.isActive) continuation.resume(Result.failure(e))
                    }

                    override fun onCodeSent(
                        verificationId: String,
                        token: PhoneAuthProvider.ForceResendingToken
                    ) {
                        if (continuation.isActive) continuation.resume(Result.success(verificationId))
                    }
                })
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }

    override suspend fun verifyOtp(verificationId: String, otp: String): Result<User?> {
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, otp)
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: return Result.success(null)
            val existingUser = userRepository.getUser(firebaseUser.uid).getOrNull()
            Result.success(existingUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginWithEmail(email: String, password: String): Result<User?> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: return Result.success(null)
            Timber.d("Email login success. UID: ${firebaseUser.uid}")
            val existingUser = userRepository.getUser(firebaseUser.uid).getOrNull()
            Result.success(existingUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun registerWithEmail(email: String, password: String): Result<String> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: return Result.failure(Exception("Registration failed"))
            authResult.user?.sendEmailVerification()?.await()
            Result.success(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendEmailVerification(): Result<Unit> {
        return try {
            auth.currentUser?.sendEmailVerification()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isEmailVerified(): Boolean {
        return auth.currentUser?.isEmailVerified == true
    }

    suspend fun reloadUser() {
        auth.currentUser?.reload()?.await()
    }

    override suspend fun getCurrentUser(): User? {
        val uid = auth.currentUser?.uid ?: return null
        return userRepository.getUser(uid).getOrNull()
    }

    override fun isLoggedIn(): Boolean = auth.currentUser != null

    override fun getCurrentUid(): String? = auth.currentUser?.uid

    override fun logout() = auth.signOut()
}
