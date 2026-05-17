package eu.vendeli.rethis.api.processor.utils

import eu.vendeli.rethis.api.processor.context.RSpecRaw
import eu.vendeli.rethis.api.processor.context.SpecResponses
import eu.vendeli.rethis.api.processor.core.RedisCommandProcessor.Companion.context
import eu.vendeli.rethis.api.processor.types.RedisCommandApiSpec
import eu.vendeli.rethis.api.processor.types.SpecBundle
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * Loads the unified Redis API spec from `vendelieu/redis-spec`. The artifact is a single JSON
 * file that bundles every upstream source (`redis/redis@unstable`, `redis/docs@main`, all four
 * Redis Stack module specs) plus structured `ReplyShape` values per command per protocol.
 *
 * Tracks the spec repo's default branch — every codegen run picks up the freshest published
 * artifact. The actual `redisRepoSha` / `redisDocsSha` it was built from are recorded inside
 * [SpecManifest] so any divergence between builds is auditable from the bundle itself.
 */
internal object RedisSpecLoader {
    private val json = Json {
        ignoreUnknownKeys = true
        classDiscriminator = "kind"
    }
    private val client = HttpClient.newBuilder().build()

    private const val SPEC_REF = "master"
    private const val SPEC_URL =
        "https://raw.githubusercontent.com/vendelieu/redis-spec/$SPEC_REF/output/spec.json"

    fun loadSpecs() {
        val bundle = fetchBundle()
        val sentinelOverlay = loadSentinelOverlay()
        val merged = (bundle.commands + sentinelOverlay).toMutableMap()

        context += RSpecRaw(merged)
        context += SpecResponses(merged.mapValues { (_, spec) -> spec.replies.toRespCodes() })
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun fetchBundle(): SpecBundle = runCatching {
        val response = client.send(
            HttpRequest.newBuilder().uri(URI.create(SPEC_URL)).build(),
            HttpResponse.BodyHandlers.ofInputStream(),
        )
        response.body().use { json.decodeFromStream<SpecBundle>(it) }
    }.getOrElse {
        throw RuntimeException("Failed to load Redis spec bundle from $SPEC_URL: ${it.message}", it)
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun loadSentinelOverlay(): Map<String, RedisCommandApiSpec> = runCatching {
        val resource = javaClass.classLoader.getResourceAsStream("sentinel_spec.json")
            ?: return@runCatching emptyMap()
        resource.use { json.decodeFromStream<Map<String, RedisCommandApiSpec>>(it) }
    }.getOrElse {
        throw RuntimeException("Failed to load Sentinel overlay: ${it.message}", it)
    }
}
