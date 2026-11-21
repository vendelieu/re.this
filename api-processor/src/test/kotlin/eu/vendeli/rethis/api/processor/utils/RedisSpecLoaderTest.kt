package eu.vendeli.rethis.api.processor.utils

import java.io.ByteArrayInputStream
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RedisSpecLoaderTest {
    @Test
    fun `spec url uses the pinned ref from the build-generated resource and not master`() {
        val ref = RedisSpecLoader.pinnedRef()

        val url = RedisSpecLoader.specUrl(ref)

        assertTrue(ref.matches(Regex("[0-9a-f]{40}")), "expected a commit SHA but got $ref")
        assertContains(url, "/redis-spec/$ref/output/spec.json")
        assertFalse(url.contains("/master/"))
    }

    @Test
    fun `non-200 status fails with the url and the status code`() {
        val url = RedisSpecLoader.specUrl("deadbeef")
        val fetcher = RedisSpecLoader.SpecFetcher {
            RedisSpecLoader.SpecResponse(404, ByteArrayInputStream("Not Found".toByteArray()))
        }

        val error = assertFailsWith<RuntimeException> { RedisSpecLoader.fetchBundle(url, fetcher) }

        val message = error.message.orEmpty()
        assertContains(message, url)
        assertContains(message, "404")
    }
}
