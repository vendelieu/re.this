package eu.vendeli.rethis.api.processor.type

import kotlinx.serialization.Serializable

@Serializable
internal data class RedisCommandFullSpec(
    val commands: Map<String, RedisCommandApiSpec>,
    val resp2Responses: Map<String, List<String>>,
    val resp3Responses: Map<String, List<String>>,
)
