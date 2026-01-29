package com.example.otpauth.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.otpauth.viewmodel.AuthUiState
import com.example.otpauth.viewmodel.AuthViewModel

@Composable
fun AuthApp(viewModel: AuthViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (val state = uiState) {
            is AuthUiState.InputEmail -> LoginScreen(
                onSendOtp = { viewModel.sendOtp(it) }
            )
            is AuthUiState.InputOtp -> OtpScreen(
                email = state.email,
                error = state.error,
                remainingTime = state.remainingTimeSeconds,
                onVerify = { viewModel.verifyOtp(it) },
                onResend = { viewModel.resendOtp() }
            )
            is AuthUiState.LoggedIn -> SessionScreen(
                email = state.email,
                duration = state.sessionDuration,
                onLogout = { viewModel.logout() }
            )
        }
    }
}

@Composable
fun LoginScreen(onSendOtp: (String) -> Unit) {
    var email by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Welcome", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onSendOtp(email) },
            enabled = email.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Send OTP")
        }
    }
}

@Composable
fun OtpScreen(
    email: String,
    error: String?,
    remainingTime: Long,
    onVerify: (String) -> Unit,
    onResend: () -> Unit
) {
    var otp by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Enter OTP", style = MaterialTheme.typography.headlineMedium)
        Text(text = "Sent to $email", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = otp,
            onValueChange = { if (it.length <= 6) otp = it },
            label = { Text("6-Digit OTP") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (error != null) {
            Text(text = error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Expires in: ${remainingTime}s")

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onVerify(otp) },
            enabled = otp.length == 6,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Verify")
        }

        if (remainingTime <= 0) {
            TextButton(onClick = onResend) {
                Text("Resend OTP")
            }
        }
    }
}

@Composable
fun SessionScreen(email: String, duration: String, onLogout: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Session Active", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "User: $email")

        Spacer(modifier = Modifier.height(32.dp))

        Text(text = duration, style = MaterialTheme.typography.displayLarge)
        Text(text = "Session Duration", style = MaterialTheme.typography.labelMedium)

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Logout")
        }
    }
}