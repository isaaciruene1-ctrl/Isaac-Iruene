package com.example.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class AiCapabilityRegistryTest {

    @Before
    fun setUp() {
        AiCapabilityRegistry.resetForTesting()
    }

    @Test
    fun billingErrorOnImageGeneration_hidesImageGenWhileKeepingTextAndVision() {
        val error = IllegalStateException("Gemini image model requires a Firebase Blaze billing plan (403 PERMISSION_DENIED)")

        val removed = AiCapabilityRegistry.reportPermanentFailure(
            kind = AiCapabilityKind.IMAGE_GENERATION,
            throwable = error
        )

        assertTrue(removed)
        val caps = AiCapabilityRegistry.capabilities.value
        assertFalse(caps.imageGenAvailable)
        assertTrue(caps.textAndVisionAvailable)
        assertTrue(caps.anyAiAvailable)
        assertNotNull(caps.removedNotice)
    }

    @Test
    fun quotaAndApiDisabledOnTextAndVision_hidesTextAndVision() {
        val nested = RuntimeException(
            "Request failed",
            IllegalStateException("429 RESOURCE_EXHAUSTED: Quota exceeded for free tier")
        )

        val removed = AiCapabilityRegistry.reportPermanentFailure(
            kind = AiCapabilityKind.TEXT_AND_VISION,
            throwable = nested
        )

        assertTrue(removed)
        val caps = AiCapabilityRegistry.capabilities.value
        assertFalse(caps.textAndVisionAvailable)
        assertTrue(caps.imageGenAvailable)
    }

    @Test
    fun appCheckOrServiceDisabled_disablesRequestedCapability() {
        AiCapabilityRegistry.reportPermanentFailure(
            AiCapabilityKind.IMAGE_GENERATION,
            RuntimeException("403 App Check attestation invalid")
        )
        AiCapabilityRegistry.reportPermanentFailure(
            AiCapabilityKind.TEXT_AND_VISION,
            RuntimeException("SERVICE_DISABLED: API has not been used in project")
        )

        val caps = AiCapabilityRegistry.capabilities.value
        assertFalse(caps.imageGenAvailable)
        assertFalse(caps.textAndVisionAvailable)
        assertFalse(caps.anyAiAvailable)
    }

    @Test
    fun transientNetworkErrors_doNotDisableAiCapabilities() {
        val dnsFailure = UnknownHostException("Unable to resolve host \"firebasevertexai.googleapis.com\"")
        val timeoutFailure = SocketTimeoutException("SocketTimeoutException: failed to connect")

        assertFalse(AiCapabilityRegistry.reportPermanentFailure(AiCapabilityKind.IMAGE_GENERATION, dnsFailure))
        assertFalse(AiCapabilityRegistry.reportPermanentFailure(AiCapabilityKind.TEXT_AND_VISION, timeoutFailure))

        val caps = AiCapabilityRegistry.capabilities.value
        assertTrue(caps.imageGenAvailable)
        assertTrue(caps.textAndVisionAvailable)
        assertTrue(caps.anyAiAvailable)
        assertNull(caps.removedNotice)
    }

    @Test
    fun consumeNotice_clearsBannerWithoutReenablingRemovedCapability() {
        AiCapabilityRegistry.reportPermanentFailure(
            AiCapabilityKind.IMAGE_GENERATION,
            RuntimeException("Billing must be enabled on this project")
        )
        assertNotNull(AiCapabilityRegistry.capabilities.value.removedNotice)

        AiCapabilityRegistry.consumeNotice()

        val after = AiCapabilityRegistry.capabilities.value
        assertNull(after.removedNotice)
        assertFalse(after.imageGenAvailable)
        assertEquals(true, after.textAndVisionAvailable)
    }
}
