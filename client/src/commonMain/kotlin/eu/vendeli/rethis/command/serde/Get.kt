package eu.vendeli.rethis.command.serde

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.shared.types.DataProcessingException
import eu.vendeli.rethis.types.interfaces.SerializationFormat
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

suspend inline fun <reified T> ReThis.get(
    key: String,
): T? where T : Any {
    return get(key, serializer<T>())
}

suspend fun <T> ReThis.get(
    key: String,
    serializer: KSerializer<T>,
): T? {
    val s: String = get(key) ?: return null
    return try {
        cfg.serializationFormat.deserialize(serializer, s)
    } catch (ex: Exception) {
        throw DataProcessingException("Failed to deserialize value for key \"$key\"", ex)
    }
}

suspend fun <T> ReThis.get(
    key: String,
    serializer: KSerializer<T>,
    format: SerializationFormat,
): T? {
    val s: String = get(key) ?: return null
    return try {
        format.deserialize(serializer, s)
    } catch (ex: Exception) {
        throw DataProcessingException("Failed to deserialize value for key \"$key\"", ex)
    }
}
