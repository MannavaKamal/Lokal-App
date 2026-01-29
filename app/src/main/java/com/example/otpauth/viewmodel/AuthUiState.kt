package com.example.otpauth.viewmodel

sealed interface AuthUiState {
     object InputEmail : AuthUiState

    data class InputOtp(
        val email: String,
        val error: String? = null,
        val remainingTimeSeconds: Long = 60
    ) : AuthUiState

    data class LoggedIn(
        val email: String,
        val sessionDuration: String = "00:00"
    ) : AuthUiState
}