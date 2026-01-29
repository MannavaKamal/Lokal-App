package com.example.otpauth.data

import kotlin.random.Random

data class OtpData(
    val code: String,
    val timestamp: Long,
    var attempts: Int = 0
)

class OtpManager {
    // In-memory storage per email
    private val otpStorage = mutableMapOf<String, OtpData>()

    companion object {
        private const val OTP_EXPIRY_MS = 60_000L // 60 seconds
        private const val MAX_ATTEMPTS = 3
    }

    fun generateOtp(email: String): String {
        // Generate 6 digit code
        val code = Random.nextInt(100000, 999999).toString()
        // Store, invalidating previous
        otpStorage[email] = OtpData(code, System.currentTimeMillis(), 0)
        return code
    }

    sealed class ValidationResult {
        object Success : ValidationResult()
        object Expired : ValidationResult()
        object Invalid : ValidationResult()
        object MaxAttemptsExceeded : ValidationResult()
        object NoOtpFound : ValidationResult()
    }

    fun validateOtp(email: String, inputCode: String): ValidationResult {
        val data = otpStorage[email] ?: return ValidationResult.NoOtpFound

        // Check Expiry
        if (System.currentTimeMillis() - data.timestamp > OTP_EXPIRY_MS) {
            otpStorage.remove(email) // Clean up
            return ValidationResult.Expired
        }

        // Check Attempts
        if (data.attempts >= MAX_ATTEMPTS) {
            otpStorage.remove(email) // Invalidate on max attempts
            return ValidationResult.MaxAttemptsExceeded
        }

        // Check Match
        if (data.code == inputCode) {
            otpStorage.remove(email) // Consume OTP on success
            return ValidationResult.Success
        } else {
            data.attempts++
            return ValidationResult.Invalid
        }
    }

    fun getRemainingTime(email: String): Long {
        val data = otpStorage[email] ?: return 0L
        val elapsed = System.currentTimeMillis() - data.timestamp
        return (OTP_EXPIRY_MS - elapsed).coerceAtLeast(0L)
    }
}