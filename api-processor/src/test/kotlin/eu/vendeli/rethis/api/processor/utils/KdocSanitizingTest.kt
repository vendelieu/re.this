package eu.vendeli.rethis.api.processor.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class KdocSanitizingTest {
    @Test
    fun `comment delimiters are neutralised`() {
        assertEquals("ends *&#47; here and &#47;* nests", sanitizeKdoc("ends */ here and /* nests"))
        assertEquals("&#47;*&#47;", sanitizeKdoc("/*/"))
    }

    @Test
    fun `text without delimiters is unchanged`() {
        val doc = "@param timeout in milliseconds; `0` blocks forever."
        assertEquals(doc, sanitizeKdoc(doc))
    }
}
