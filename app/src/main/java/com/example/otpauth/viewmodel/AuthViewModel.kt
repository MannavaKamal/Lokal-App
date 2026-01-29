package com.example.otpauth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.otpauth.analytics.AnalyticsLogger
import com.example.otpauth.analytics.TimberAnalyticsLogger
import com.example.otpauth.data.OtpManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber

class AuthViewModel(
    // In a real app, inject these
    private val otpManager: OtpManager = OtpManager(),
    private val analytics: AnalyticsLogger = TimberAnalyticsLogger()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.InputEmail)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var sessionJob: Job? = null
    private var otpTimerJob: Job? = null
    private var sessionStartTime: Long = 0L

    fun sendOtp(email: String) {
        if (email.isBlank()) return // Simple validation

        val otp = otpManager.generateOtp(email)
        // Log for debugging/demo purposes since we don't have an email server
        Timber.i("OTP Generated for $email: $otp")

        analytics.logEvent("otp_generated", mapOf("email" to email))

        _uiState.value = AuthUiState.InputOtp(email = email)
        startOtpCountdown(email)
    }

    fun verifyOtp(inputOtp: String) {
        val currentState = _uiState.value
        if (currentState !is AuthUiState.InputOtp) return

        val result = otpManager.validateOtp(currentState.email, inputOtp)

        when (result) {
            OtpManager.ValidationResult.Success -> {
                analytics.logEvent("otp_validation_success")
                startSession(currentState.email)
            }
            OtpManager.ValidationResult.Invalid -> {
                analytics.logEvent("otp_validation_failure", mapOf("reason" to "invalid_code"))
                _uiState.update { currentState.copy(error = "Incorrect OTP. Try again.") }
            }
            OtpManager.ValidationResult.Expired -> {
                analytics.logEvent("otp_validation_failure", mapOf("reason" to "expired"))
                _uiState.update { currentState.copy(error = "OTP Expired. Please resend.") }
            }
            OtpManager.ValidationResult.MaxAttemptsExceeded -> {
                analytics.logEvent("otp_validation_failure", mapOf("reason" to "max_attempts"))
                _uiState.update { currentState.copy(error = "Max attempts reached. Resend OTP.") }
            }
            OtpManager.ValidationResult.NoOtpFound -> {
                _uiState.value = AuthUiState.InputEmail
            }
        }
    }

    fun resendOtp() {
        val currentState = _uiState.value
        if (currentState is AuthUiState.InputOtp) {
            sendOtp(currentState.email)
        }
    }

    private fun startSession(email: String) {
        otpTimerJob?.cancel() // Stop OTP timer
        sessionStartTime = System.currentTimeMillis()

        // Initialize LoggedIn state
        _uiState.value = AuthUiState.LoggedIn(email, "00:00")

        // Start session ticker
        sessionJob = viewModelScope.launch {
            while (isActive) {
                val elapsed = System.currentTimeMillis() - sessionStartTime
                val seconds = (elapsed / 1000) % 60
                val minutes = (elapsed / (1000 * 60)) % 60
                val durationString = String.format("%02d:%02d", minutes, seconds)

                _uiState.update {
                    if (it is AuthUiState.LoggedIn) it.copy(sessionDuration = durationString) else it
                }
                delay(1000)
            }
        }
    }

    private fun startOtpCountdown(email: String) {
        otpTimerJob?.cancel()
        otpTimerJob = viewModelScope.launch {
            while (isActive) {
                val remaining = otpManager.getRemainingTime(email) / 1000
                _uiState.update {
                    if (it is AuthUiState.InputOtp) it.copy(remainingTimeSeconds = remaining) else it
                }
                if (remaining <= 0) break
                delay(1000)
            }
        }
    }

    fun logout() {
        sessionJob?.cancel()
        analytics.logEvent("logout")
        _uiState.value = AuthUiState.InputEmail
    }
}