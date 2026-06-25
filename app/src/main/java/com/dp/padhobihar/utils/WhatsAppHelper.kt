package com.dp.padhobihar.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object WhatsAppHelper {

    fun openChat(context: Context, phone: String, message: String = "") {
        if (phone.isBlank()) {
            Toast.makeText(context, "Agent phone number not available", Toast.LENGTH_SHORT).show()
            return
        }
        val formattedPhone = "91${phone.takeLast(10)}"
        val uri = Uri.parse("https://wa.me/$formattedPhone?text=${Uri.encode(message)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
        }
    }
}
