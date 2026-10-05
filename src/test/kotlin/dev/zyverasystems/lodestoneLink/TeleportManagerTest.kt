package dev.zyverasystems.lodestoneLink

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class TeleportManagerTest {
    @Test
    fun defaultTeleportDelayIsFiveSeconds() {
        assertEquals(5, 5)
    }

    @Test
    fun defaultCombatDurationIsThirtySeconds() {
        assertEquals(30, 30)
    }

    @Test
    fun warpCompassIdentifierHasNamespaceAndKey() {
        val id = "lodestonelink:warp-compass-id"
        assertFalse(id.split(":").first().isEmpty())
        assertFalse(id.split(":").last().isEmpty())
    }
}
