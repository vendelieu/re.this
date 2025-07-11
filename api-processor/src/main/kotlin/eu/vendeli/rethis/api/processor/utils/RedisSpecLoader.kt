package eu.vendeli.rethis.api.processor.utils

import eu.vendeli.rethis.api.processor.context.RSpecRaw
import eu.vendeli.rethis.api.processor.context.SpecResponses
import eu.vendeli.rethis.api.processor.core.RedisCommandProcessor.Companion.context
import eu.vendeli.rethis.api.processor.types.RedisCommandApiSpec
import eu.vendeli.rethis.api.spec.common.types.RespCode
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

internal object RedisSpecLoader {
    private val json = Json {
        ignoreUnknownKeys = true
    }
    private val client = HttpClient.newBuilder().build()
    private const val SPEC_BASE_URL =
        "https://raw.githubusercontent.com/redis/docs/4b1ff1c1c8a6d6b6f70c64d4f0aa091fc0fe00a5/data/"

    fun loadSpecs() {
        enrichContextWithCommands()
        enrichContextWithResponses()
    }

    private fun enrichContextWithResponses() = runCatching {
        val r2Responses = loadResponses("resp2_replies.json")
        val r3Responses = loadResponses("resp3_replies.json")

        val allResponses = mutableMapOf<String, Set<RespCode>>()

        r2Responses.forEach { cmd, responses ->
            allResponses[cmd] = responses.mapNotNull { it.inferResponseType() }.toSet()
        }

        r3Responses.forEach { cmd, responses ->
            allResponses[cmd] = responses.mapNotNull { it.inferResponseType() }.toSet()
        }

        context += SpecResponses(allResponses)
    }.getOrElse {
        throw RuntimeException("Failed to load Redis response specs: ${it.message}", it)
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun enrichContextWithCommands() = runCatching {
        val commands = loadCommands().apply {
            putAll(loadCommands("commands_redisjson.json"))
        }

        val sentinelRes = javaClass.classLoader.getResourceAsStream("sentinel_spec.json")!!
        commands.putAll(json.decodeFromStream<MutableMap<String, RedisCommandApiSpec>>(sentinelRes))

        context += RSpecRaw(commands)
    }.getOrElse {
        throw RuntimeException("Failed to load Redis command specs: ${it.message}", it)
    }

    private fun loadCommands(module: String = "commands.json"): MutableMap<String, RedisCommandApiSpec> {
        val response = client.send(
            HttpRequest.newBuilder()
                .uri(URI.create(SPEC_BASE_URL + module))
                .build(),
            HttpResponse.BodyHandlers.ofString(),
        )
        return json.decodeFromString(response.body())
    }

    private fun loadResponses(url: String): Map<String, List<String>> {
        val response = client.send(
            HttpRequest.newBuilder()
                .uri(URI.create(SPEC_BASE_URL + url))
                .build(),
            HttpResponse.BodyHandlers.ofString(),
        )
        return json.decodeFromString(response.body())
    }
}
