package eu.vendeli.rethis.command.serde

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.command.string.get
import eu.vendeli.rethis.shared.types.DataProcessingException
import eu.vendeli.rethis.types.interfaces.SerializationFormat
import eu.vendeli.rethis.utils.isInTx
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

suspend inline fun <reified T> ReThis.get(
    key: String,
): T? where T : Any = get(key, serializer<T>())

suspend fun <T> ReThis.get(
    key: String,
    serializer: KSerializer<T>,
    format: SerializationFormat = cfg.serializationFormat,
): T? {
    if (isInTx()) {
        logger.warn(
            "Be aware that in transaction commands return `QUEUED`" +
                " which is for type safety substituted with default value, so serde operations will fail",
        )
    }
    val s: String = get(key) ?: return null
    return try {
        format.deserialize(serializer, s)
    } catch (ex: Exception) {
        throw DataProcessingException("Failed to deserialize value for key \"$key\"", ex)
    }
}
