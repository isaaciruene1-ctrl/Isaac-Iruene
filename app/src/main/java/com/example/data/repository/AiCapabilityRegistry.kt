package com.example.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class AiCapabilityKind {
    IMAGE_GENERATION,
    TEXT_AND_VISION
}

data class AiCapabilities(
    val textAndVisionAvailable: Boolean = true,
    val imageGenAvailable: Boolean = true,
    val removedNotice: String? = null
) {
    val anyAiAvailable: Boolean
        get() = textAndVisionAvailable || imageGenAvailable
}

/**
 * Tracks live Firebase AI Logic feature availability across the session.
 *
 * When a cloud AI call fails because of Blaze billing requirements, quota exhaustion,
 * or disabled backend APIs (as opposed to transient offline/network blips), the blocked
 * cloud AI capability is cleanly deactivated so its UI surfaces disappear while the
 * on-device Depth Detection Engine remains permanently active.
 */
object AiCapabilityRegistry {

    private val _capabilities = MutableStateFlow(AiCapabilities())
    val capabilities: StateFlow<AiCapabilities> = _capabilities.asStateFlow()

    fun isPermanentBillingOrQuotaError(throwable: Throwable): Boolean {
        val chainMessages = buildString {
            var current: Throwable? = throwable
            var depth = 0
            while (current != null && depth < 6) {
                append(' ')
                append(current.javaClass.simpleName)
                append(' ')
                append(current.message.orEmpty())
                current = current.cause
                depth++
            }
        }.lowercase()

        // Explicit network connectivity failures remain retryable, not permanent removals
        val isTransientNetwork = chainMessages.contains("unknownhost") ||
            chainMessages.contains("unable to resolve host") ||
            chainMessages.contains("sockettimeout") ||
            chainMessages.contains("failed to connect") ||
            chainMessages.contains("connection reset") ||
            chainMessages.contains("network is unreachable")
        if (isTransientNetwork) return false

        val classHit = throwable.javaClass.simpleName.lowercase().let { name ->
            name.contains("quota") || name.contains("authorization") || name.contains("permission")
        }

        val messageHit = chainMessages.contains("billing") ||
            chainMessages.contains("blaze") ||
            chainMessages.contains("pay-as-you-go") ||
            chainMessages.contains("quota") ||
            chainMessages.contains("resource_exhausted") ||
            chainMessages.contains("rate limit") ||
            chainMessages.contains("exceeded") ||
            chainMessages.contains("402") ||
            chainMessages.contains("403") ||
            chainMessages.contains("429") ||
            chainMessages.contains("permission_denied") ||
            chainMessages.contains("permission denied") ||
            chainMessages.contains("failed_precondition") ||
            chainMessages.contains("api has not been used") ||
            chainMessages.contains("is not enabled") ||
            chainMessages.contains("service_disabled") ||
            chainMessages.contains("app check") ||
            chainMessages.contains("appcheck") ||
            chainMessages.contains("attestation") ||
            chainMessages.contains("free tier") ||
            chainMessages.contains("not available in your") ||
            chainMessages.contains("not supported for this project")

        return classHit || messageHit
    }

    fun reportPermanentFailure(kind: AiCapabilityKind, throwable: Throwable): Boolean {
        if (!isPermanentBillingOrQuotaError(throwable)) return false
        _capabilities.update { current ->
            when (kind) {
                AiCapabilityKind.IMAGE_GENERATION -> current.copy(
                    imageGenAvailable = false,
                    removedNotice = current.removedNotice
                        ?: "Gemini image tools require an active Blaze quota and were hidden. Depth Engine stays active."
                )
                AiCapabilityKind.TEXT_AND_VISION -> current.copy(
                    textAndVisionAvailable = false,
                    removedNotice = "Cloud AI features are unavailable for this Firebase billing/quota tier and were hidden. Depth Engine stays active."
                )
            }
        }
        return true
    }

    fun consumeNotice() {
        _capabilities.update { it.copy(removedNotice = null) }
    }

    fun resetForTesting() {
        _capabilities.value = AiCapabilities()
    }
}
