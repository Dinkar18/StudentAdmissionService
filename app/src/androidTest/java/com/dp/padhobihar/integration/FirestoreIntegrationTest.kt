package com.dp.padhobihar.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.*

/**
 * Integration tests using Firebase Emulator.
 * Run with: firebase emulators:start (Firestore on port 8080)
 * Then: ./gradlew connectedAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class FirestoreIntegrationTest {

    private lateinit var firestore: FirebaseFirestore

    @Before
    fun setup() {
        firestore = FirebaseFirestore.getInstance()
        // Connect to emulator (10.0.2.2 is host from Android emulator)
        firestore.useEmulator("10.0.2.2", 8080)
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(false)
            .build()
    }

    @Test
    fun createAndReadStudent() = runBlocking {
        val data = hashMapOf(
            "name" to "Test Student",
            "phone" to "9999999999",
            "agentId" to "agent123",
            "status" to "REGISTERED"
        )
        val doc = firestore.collection("students").add(data).await()
        val read = firestore.collection("students").document(doc.id).get().await()
        assertEquals("Test Student", read.getString("name"))
        // Cleanup
        firestore.collection("students").document(doc.id).delete().await()
    }

    @Test
    fun studentStatusUpdate() = runBlocking {
        val data = hashMapOf("name" to "Status Test", "status" to "REGISTERED")
        val doc = firestore.collection("students").add(data).await()
        firestore.collection("students").document(doc.id).update("status", "PROFILE_COMPLETE").await()
        val read = firestore.collection("students").document(doc.id).get().await()
        assertEquals("PROFILE_COMPLETE", read.getString("status"))
        firestore.collection("students").document(doc.id).delete().await()
    }

    @Test
    fun collegeAddAndRetrieve() = runBlocking {
        val data = hashMapOf("name" to "Test College", "district" to "Patna")
        firestore.collection("colleges").document("TEST001").set(data).await()
        val read = firestore.collection("colleges").document("TEST001").get().await()
        assertEquals("Test College", read.getString("name"))
        firestore.collection("colleges").document("TEST001").delete().await()
    }
}
