package eu.vendeli.rethis.command.serde

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.command.string.mGet
import eu.vendeli.rethis.shared.types.DataProcessingException
import eu.vendeli.rethis.types.interfaces.SerializationFormat
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

suspend inline fun <reified T : Any> ReThis.mGet(
    vararg key: String,
): List<T?> = mGet(key = key, serializer = serializer<T>())

suspend fun <T : Any> ReThis.mGet(
    vararg key: String,
    serializer: KSerializer<T>,
    format: SerializationFormat = cfg.serializationFormat,
): List<T?> {
    val raw: List<String?> = mGet(*key)
    return raw.map { string ->
        if (string == null) return@map null
        try {
            format.deserialize(serializer, string)
        } catch (ex: Exception) {
            throw DataProcessingException("Failed to deserialize value for mGet", ex)
        }
    }
}

