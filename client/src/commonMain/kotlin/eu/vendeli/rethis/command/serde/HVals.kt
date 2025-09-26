package eu.vendeli.rethis.command.serde

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.command.hash.hVals
import eu.vendeli.rethis.shared.types.DataProcessingException
import eu.vendeli.rethis.types.interfaces.SerializationFormat
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

suspend inline fun <reified T : Any> ReThis.hVals(
    key: String,
): List<T> = hVals(key, serializer<T>())

suspend fun <T : Any> ReThis.hVals(
    key: String,
    serializer: KSerializer<T>,
    format: SerializationFormat = cfg.serializationFormat,
): List<T> {
    val raw: List<String> = hVals(key)
    return raw.map { string ->
        try {
            format.deserialize(serializer, string)
        } catch (ex: Exception) {
            throw DataProcessingException("Failed to deserialize hVals for key \"$key\"", ex)
        }
    }
}
