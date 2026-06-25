package com.dp.padhobihar.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Secure credential storage using EncryptedSharedPreferences.
 * Falls back to regular SharedPreferences if encryption fails.
 */
object SecurePrefs {

    private const val FILE_NAME = "padho_bihar_secure_prefs"

    private fun getPrefs(context: Context): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context, FILE_NAME, masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        }
    }

    fun saveAdminCredentials(context: Context, email: String, password: String) {
        getPrefs(context).edit().putString("email", email).putString("pwd", password).apply()
    }

    fun getAdminEmail(context: Context): String = getPrefs(context).getString("email", "") ?: ""
    fun getAdminPassword(context: Context): String = getPrefs(context).getString("pwd", "") ?: ""

    fun clear(context: Context) = getPrefs(context).edit().clear().apply()
}
