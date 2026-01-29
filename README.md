# OTP Authentication & Session Manager

## Overview
This Android application demonstrates a passwordless Email + OTP login flow and a session timer using Jetpack Compose and Modern Android Architecture.

## 1. OTP Logic and Expiry Handling
The logic is encapsulated in `OtpManager.kt`.
- **Data Structure:** I used a `MutableMap<String, OtpData>` where the key is the email address. This ensures quick O(1) lookups and naturally supports storing OTPs per email.
- **Expiry:** The `OtpData` class stores a `timestamp` (system time at generation). During validation, `System.currentTimeMillis()` is compared against `timestamp + 60_000`. If elapsed time > 60s, the OTP is considered expired.
- **Attempts:** An `attempts` counter is stored in the value object. It increments on failure and invalidates the OTP if it hits 3.
- **Invalidation:** Generating a new OTP for an email overwrites the map entry, automatically invalidating the old code.

## 2. External SDK: Timber
I chose **Timber** for this assignment.
- **Why:** The requirements allowed for Timber, Firebase, or Sentry. Timber is lightweight, requires no API keys or configuration files (like `google-services.json`), and is ideal for demonstrating SDK integration patterns without setup friction for the reviewer.
- **Integration:** Added via Gradle, initialized in `AnalyticsLogger`, and wrapped in an interface to adhere to dependency inversion principles.

## 3. Architecture & Tech Stack
- **MVVM:** `AuthViewModel` holds the business logic and exposes `StateFlow` to the UI.
- **UI State:** A sealed interface `AuthUiState` manages screen transitions (InputEmail -> InputOtp -> LoggedIn). This ensures the UI is always in a valid, deterministic state.
- **Coroutines:** Used for the countdown timer (OTP expiry) and the session duration timer.
- **State Hoisting:** All state is hoisted to the ViewModel; Composables are stateless and receive data/callbacks.

## 4. AI Assistance Statement
- **Generated:** Boilerplate Compose code (modifiers, padding) and the basic `README` structure.
- **Self-Implemented:** The specific `OtpManager` logic, the `StateFlow` architecture in the ViewModel, and the handling of the Coroutine timers to ensure they survive recomposition but cancel correctly on logout.
