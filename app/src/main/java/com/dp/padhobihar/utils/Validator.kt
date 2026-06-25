package com.dp.padhobihar.utils

object Validator {

    fun isValidEmail(email: String): Boolean =
        email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

    fun isValidPhone(phone: String): Boolean =
        phone.length == 10 && phone.all { it.isDigit() }

    fun isValidPassword(password: String): Boolean =
        password.length >= 6

    fun isValidName(name: String): Boolean =
        name.isNotBlank() && name.length in 2..100

    fun isValidReferralCode(code: String): Boolean =
        code.isNotBlank() && code.length in 4..10 && code.all { it.isLetterOrDigit() }

    fun sanitize(input: String): String =
        input.trim()
            .replace("<", "")
            .replace(">", "")
            .replace("\"", "")
            .replace("'", "")
            .take(500)
}
