package eu.vendeli.rethis.api.processor.utils

import eu.vendeli.rethis.api.processor.type.RedisCommandApiSpec
import eu.vendeli.rethis.api.processor.type.RedisCommandFullSpec
import io.ktor.util.logging.*
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

internal object RedisSpecLoader {
    private val logger = KtorSimpleLogger(javaClass.name)
    private val json = Json {
        ignoreUnknownKeys = true
    }
    private val client = HttpClient.newBuilder().build()
    private const val SPEC_BASE_URL = "https://raw.githubusercontent.com/redis/docs/refs/heads/main/data/"

    fun loadSpecs(): RedisCommandFullSpec = runCatching {
        val commands = loadCommands().apply {
            putAll(loadCommands("commands_redisjson.json"))
        }

        val resp2 = loadResponses("resp2_replies.json")
        val resp3 = loadResponses("resp3_replies.json")

        logger.info("Loaded ${commands.size} commands and RESP2 ${resp2.size} + RESP3 ${resp3.size}")

        RedisCommandFullSpec(commands, resp2, resp3)
    }.getOrElse {
        throw RuntimeException("Failed to load Redis specs: ${it.message}", it)
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
