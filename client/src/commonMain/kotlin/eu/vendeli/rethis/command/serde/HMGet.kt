package eu.vendeli.rethis.command.serde

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.command.hash.hMGet
import eu.vendeli.rethis.shared.types.DataProcessingException
import eu.vendeli.rethis.types.interfaces.SerializationFormat
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer


suspend inline fun <reified T : Any> ReThis.hMGet(
    key: String,
    vararg field: String,
): List<T?> = hMGet(key = key, field = field, serializer = serializer<T>())

suspend fun <T : Any> ReThis.hMGet(
    key: String,
    vararg field: String,
    serializer: KSerializer<T>,
    format: SerializationFormat = cfg.serializationFormat,
): List<T?> {
    val raw: List<String?> = hMGet(key, *field)
    return raw.map { string ->
        if (string == null) return@map null
        try {
            format.deserialize(serializer, string)
        } catch (ex: Exception) {
            throw DataProcessingException("Failed to deserialize hMGet for key \"$key\"", ex)
        }
    }
}
