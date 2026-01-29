package com.example.otpauth.analytics

import timber.log.Timber

interface AnalyticsLogger {
    fun logEvent(eventName: String, params: Map<String, Any> = emptyMap())
}

class TimberAnalyticsLogger : AnalyticsLogger {
    init {
        // Initialize Timber (usually done in Application class, but here for visibility)
        if (Timber.treeCount == 0) {
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun logEvent(eventName: String, params: Map<String, Any>) {
        Timber.tag("Analytics").d("Event: $eventName | Params: $params")
    }
}