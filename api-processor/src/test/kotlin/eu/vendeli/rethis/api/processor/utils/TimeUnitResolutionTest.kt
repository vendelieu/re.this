package eu.vendeli.rethis.api.processor.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TimeUnitResolutionTest {
    @Test
    fun `resolves qualified and bare seconds to seconds`() {
        assertEquals(OutgoingUnit.SECONDS, resolveOutgoingUnit("TimeUnit.SECONDS"))
        assertEquals(OutgoingUnit.SECONDS, resolveOutgoingUnit("SECONDS"))
    }

    @Test
    fun `resolves qualified and bare milliseconds to milliseconds`() {
        assertEquals(OutgoingUnit.MILLISECONDS, resolveOutgoingUnit("TimeUnit.MILLISECONDS"))
        assertEquals(OutgoingUnit.MILLISECONDS, resolveOutgoingUnit("MILLISECONDS"))
    }

    @Test
    fun `returns null when the annotation is absent or the unit is unsupported`() {
        assertNull(resolveOutgoingUnit(null))
        assertNull(resolveOutgoingUnit("TimeUnit.MINUTES"))
        assertNull(resolveOutgoingUnit("seconds-ish"))
    }
}
