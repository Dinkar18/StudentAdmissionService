package com.dp.padhobihar.utils

import timber.log.Timber

/**
 * Safe coroutine execution wrapper.
 * Catches all exceptions and returns Result.
 */
suspend fun <T> safeCall(tag: String = "PadhoBihar", block: suspend () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (e: Exception) {
        Timber.e(e, "Operation failed: ${e.message}")
        Result.failure(e)
    }
}
