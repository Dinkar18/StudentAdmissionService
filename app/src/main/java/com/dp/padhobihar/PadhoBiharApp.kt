package com.dp.padhobihar

import android.app.Application
import com.dp.padhobihar.data.local.CollegeDataSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltAndroidApp
class PadhoBiharApp : Application() {
    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        // Enable Firestore offline persistence
        FirebaseFirestore.getInstance().firestoreSettings =
            FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true).build()

        // Download and cache college data in background
        CoroutineScope(Dispatchers.IO).launch {
            CollegeDataSource.init(this@PadhoBiharApp)
        }
    }
}
