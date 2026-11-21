package eu.vendeli.rethis.api.processor.utils

import eu.vendeli.rethis.api.processor.context.RSpecRaw
import eu.vendeli.rethis.api.processor.context.SpecResponses
import eu.vendeli.rethis.api.processor.core.RedisCommandProcessor.Companion.context
import eu.vendeli.rethis.api.processor.types.RedisCommandApiSpec
import eu.vendeli.rethis.api.processor.types.SpecBundle
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.InputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.Properties

/**
 * Loads the unified Redis API spec from `vendelieu/redis-spec`. The artifact is a single JSON
 * file that bundles every upstream source (`redis/redis@unstable`, `redis/docs@main`, all four
 * Redis Stack module specs) plus structured `ReplyShape` values per command per protocol.
 *
 * The spec repo commit is pinned through the `redis-spec` entry of `gradle/libs.versions.toml`,
 * which the build writes to the `redis_spec_ref.properties` resource, so the same source commit
 * always generates the same codecs. The `redisRepoSha` / `redisDocsSha` the bundle was built from
 * are recorded inside [SpecManifest] and stay auditable from the bundle itself.
 */
internal object RedisSpecLoader {
    private val json = Json {
        ignoreUnknownKeys = true
        classDiscriminator = "kind"
    }
    private val CONNECT_TIMEOUT = Duration.ofSeconds(10)
    private val REQUEST_TIMEOUT = Duration.ofSeconds(60)
    private val client = HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build()

    private const val REF_RESOURCE = "redis_spec_ref.properties"
    private const val HTTP_OK = 200

    /** A downloaded spec: HTTP status plus the response body. */
    class SpecResponse(val statusCode: Int, val body: InputStream)

    fun interface SpecFetcher {
        fun fetch(url: String): SpecResponse
    }

    private val httpFetcher = SpecFetcher { url ->
        val request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(REQUEST_TIMEOUT).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofInputStream())
        SpecResponse(response.statusCode(), response.body())
    }

    internal fun specUrl(ref: String): String =
        "https://raw.githubusercontent.com/vendelieu/redis-spec/$ref/output/spec.json"

    internal fun pinnedRef(): String {
        val stream = javaClass.classLoader.getResourceAsStream(REF_RESOURCE)
            ?: error("Missing $REF_RESOURCE resource; it is generated from the redis-spec version in libs.versions.toml")
        val ref = stream.use { Properties().apply { load(it) } }.getProperty("ref")
        return ref?.takeIf { it.isNotBlank() } ?: error("$REF_RESOURCE has no 'ref' entry")
    }

    fun loadSpecs() {
        val bundle = fetchBundle()
        val sentinelOverlay = loadSentinelOverlay()
        val merged = (bundle.commands + sentinelOverlay).toMutableMap()

        context += RSpecRaw(merged)
        context += SpecResponses(merged.mapValues { (_, spec) -> spec.replies.toRespCodes() })
    }

    @OptIn(ExperimentalSerializationApi::class)
    internal fun fetchBundle(
        url: String = specUrl(pinnedRef()),
        fetcher: SpecFetcher = httpFetcher,
    ): SpecBundle = runCatching {
        val response = fetcher.fetch(url)
        response.body.use { body ->
            check(response.statusCode == HTTP_OK) { "unexpected HTTP status ${response.statusCode}" }
            json.decodeFromStream<SpecBundle>(body)
        }
    }.getOrElse {
        throw RuntimeException("Failed to load Redis spec bundle from $url: ${it.message}", it)
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
