package eu.vendeli.rethis.command.serde

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.command.json.jsonMGet
import eu.vendeli.rethis.shared.types.DataProcessingException
import eu.vendeli.rethis.types.interfaces.SerializationFormat
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

suspend inline fun <reified T : Any> ReThis.jsonMGet(
    path: String,
    vararg key: String,
): List<T?> = jsonMGet(path = path, key = key, serializer = serializer<T>())

suspend fun <T : Any> ReThis.jsonMGet(
    path: String,
    vararg key: String,
    serializer: KSerializer<T>,
    format: SerializationFormat = cfg.serializationFormat,
): List<T?> {
    val raw: List<String?> = jsonMGet(path, *key)
    return raw.map { string ->
        if (string == null) return@map null
        try {
            format.deserialize(serializer, string)
        } catch (ex: Exception) {
            throw DataProcessingException("Failed to deserialize jsonMGet for path \"$path\"", ex)
        }
    }
}
