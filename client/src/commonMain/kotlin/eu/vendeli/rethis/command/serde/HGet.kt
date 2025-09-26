package eu.vendeli.rethis.command.serde

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.command.hash.hGet
import eu.vendeli.rethis.shared.types.DataProcessingException
import eu.vendeli.rethis.types.interfaces.SerializationFormat
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

suspend inline fun <reified T : Any> ReThis.hGet(
    key: String,
    field: String,
): T? = hGet(key, field, serializer<T>())

suspend fun <T : Any> ReThis.hGet(
    key: String,
    field: String,
    serializer: KSerializer<T>,
    format: SerializationFormat = cfg.serializationFormat,
): T? {
    val raw: String = hGet(key, field) ?: return null
    return try {
        format.deserialize(serializer, raw)
    } catch (ex: Exception) {
        throw DataProcessingException("Failed to deserialize hGet for key \"$key\" field \"$field\"", ex)
    }
}

