package com.android.messaging.ui.common.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ParticipantAvatarColorTest {

    @Test
    fun participantColorSeed_ignoresWhitespaceForSameDestination() {
        assertEquals(
            participantColorSeed(normalizedDestination = "+31612345678"),
            participantColorSeed(normalizedDestination = "+31 6 1234 5678"),
        )
    }

    @Test
    fun participantColorSeed_returnsNullForBlankDestination() {
        assertNull(participantColorSeed(normalizedDestination = null))
        assertNull(participantColorSeed(normalizedDestination = "   "))
    }

    @Test
    fun participantAvatarFallbackColors_returnsStableColorsForSeed() {
        val first = participantAvatarFallbackColors(
            colorSeedCode = "dest:+31612345678",
            isDarkTheme = false,
        )
        val second = participantAvatarFallbackColors(
            colorSeedCode = "dest:+31612345678",
            isDarkTheme = false,
        )

        assertEquals(first, second)
    }

    @Test
    fun participantAvatarFallbackColors_returnsDifferentBackgroundsForDifferentSeeds() {
        val first = participantAvatarFallbackColors(
            colorSeedCode = "dest:+31612345678",
            isDarkTheme = false,
        )
        val second = participantAvatarFallbackColors(
            colorSeedCode = "conversation:42",
            isDarkTheme = false,
        )

        assertNotEquals(first.background, second.background)
    }

    @Test
    fun participantAvatarFallbackColors_returnsThemeSpecificTonePair() {
        val light = participantAvatarFallbackColors(
            colorSeedCode = "dest:+31612345678",
            isDarkTheme = false,
        )
        val dark = participantAvatarFallbackColors(
            colorSeedCode = "dest:+31612345678",
            isDarkTheme = true,
        )

        assertNotEquals(light.background, dark.background)
        assertNotEquals(light.content, dark.content)
    }
}
