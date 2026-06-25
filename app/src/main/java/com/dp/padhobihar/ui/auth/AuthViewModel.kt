package com.dp.padhobihar.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dp.padhobihar.domain.model.Role
import com.dp.padhobihar.utils.RateLimiter
import com.dp.padhobihar.domain.model.User
import com.dp.padhobihar.domain.repository.AuthRepository
import com.dp.padhobihar.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    fun loginWithRole(email: String, password: String, selectedRole: String) {
        Timber.d("loginWithRole called: role=$selectedRole")
        if (!RateLimiter.canProceed(email)) {
            _authState.value = AuthState.Error("Too many attempts. Try again in ${RateLimiter.remainingSeconds(email)} seconds.")
            return
        }
        RateLimiter.record(email)
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.loginWithEmail(email, password)
                .onSuccess { user ->
                    if (user == null) {
                        _authState.value = AuthState.Error("Account not found")
                        return@onSuccess
                    }

                    // Validate role matches selection
                    val actualRole = user.getUserRole()
                    val expectedRole = when {
                        selectedRole.contains("Admin") -> Role.ADMIN
                        selectedRole.contains("Agent") -> Role.AGENT
                        else -> Role.STUDENT
                    }

                    if (actualRole != expectedRole) {
                        auth.signOut()
                        _authState.value = AuthState.Error("You are not registered as ${expectedRole.name}. Login with correct role.")
                        return@onSuccess
                    }

                    Timber.d("loginWithRole success: ${user.role}")
                    _authState.value = AuthState.Verified(user)
                }
                .onFailure {
                    Timber.e(it, "loginWithRole failed")
                    _authState.value = AuthState.Error(it.message ?: "Login failed")
                }
        }
    }

    fun loginWithGoogle(idToken: String) {
        Timber.d("loginWithGoogle called")
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(credential).await()
                val firebaseUser = authResult.user ?: run {
                    _authState.value = AuthState.Error("Google sign-in failed")
                    return@launch
                }

                val existingUser = userRepository.getUser(firebaseUser.uid).getOrNull()
                if (existingUser != null) {
                    Timber.d("loginWithGoogle success: existing user")
                    _authState.value = AuthState.Verified(existingUser)
                } else {
                    val newUser = User(
                        id = firebaseUser.uid,
                        name = firebaseUser.displayName ?: "",
                        phone = firebaseUser.phoneNumber ?: "",
                        role = "STUDENT",
                        status = "ACTIVE"
                    )
                    userRepository.createUser(newUser)
                    Timber.d("loginWithGoogle success: new user created")
                    _authState.value = AuthState.Verified(newUser)
                }
            } catch (e: Exception) {
                Timber.e(e, "loginWithGoogle failed")
                _authState.value = AuthState.Error(e.message ?: "Google sign-in failed")
            }
        }
    }

    fun checkCurrentUser() {
        Timber.d("checkCurrentUser called")
        viewModelScope.launch {
            if (!authRepository.isLoggedIn()) {
                _authState.value = AuthState.NotLoggedIn
                return@launch
            }
            val user = authRepository.getCurrentUser()
            if (user != null) {
                Timber.d("checkCurrentUser success: ${user.role}")
                _authState.value = AuthState.Verified(user)
            } else {
                _authState.value = AuthState.NotLoggedIn
            }
        }
    }
}

sealed class AuthState {
    data object Loading : AuthState()
    data object NotLoggedIn : AuthState()
    data object OtpSent : AuthState()
    data object EmailNotVerified : AuthState()
    data class Verified(val user: User?) : AuthState()
    data class Error(val message: String) : AuthState()
}
