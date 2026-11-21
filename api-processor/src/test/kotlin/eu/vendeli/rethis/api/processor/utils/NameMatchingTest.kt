package eu.vendeli.rethis.api.processor.utils

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NameMatchingTest {
    @Test
    fun `plain and generic response decoder names are default`() {
        assertTrue(isDefaultDecoderName("ResponseDecoder"))
        assertTrue(isDefaultDecoderName("ResponseDecoder<String>"))
    }

    @Test
    fun `names merely starting with response decoder are custom`() {
        assertFalse(isDefaultDecoderName("ResponseDecoderFoo"))
    }

    @Test
    fun `kotlin and its subpackages are std packages`() {
        assertTrue(isStdPackage("kotlin"))
        assertTrue(isStdPackage("kotlin.collections"))
    }

    @Test
    fun `packages merely starting with kotlin are not std packages`() {
        assertFalse(isStdPackage("kotlinx.datetime"))
        assertFalse(isStdPackage("kotlinfoo"))
        assertFalse(isStdPackage(null))
    }
}
