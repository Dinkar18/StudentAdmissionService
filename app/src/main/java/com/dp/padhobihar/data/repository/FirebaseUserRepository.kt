package com.dp.padhobihar.data.repository

import com.dp.padhobihar.domain.model.User
import com.dp.padhobihar.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class FirebaseUserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository {

    private val usersCollection = firestore.collection("users")

    override suspend fun getUser(userId: String): Result<User?> {
        return try {
            Timber.d("Fetching user with ID: $userId")
            val doc = usersCollection.document(userId).get().await()
            Timber.d("Document exists: ${doc.exists()}, data: ${doc.data}")
            val user = doc.toObject<User>()?.copy(id = doc.id)
            Timber.d("Parsed user: $user")
            Result.success(user)
        } catch (e: Exception) {
            Timber.e(e, "getUser failed")
            Result.failure(e)
        }
    }

    override suspend fun createUser(user: User): Result<Unit> {
        return try {
            usersCollection.document(user.id).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUser(user: User): Result<Unit> {
        return try {
            usersCollection.document(user.id).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUsersByRole(role: String): Result<List<User>> {
        return try {
            val docs = usersCollection.whereEqualTo("role", role).get().await()
            Result.success(docs.mapNotNull { it.toObject<User>().copy(id = it.id) })
        } catch (e: Exception) {
            Timber.e(e, "getUsersByRole failed")
            Result.failure(e)
        }
    }
}
