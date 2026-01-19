package eu.vendeli.rethis.command.serde

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.command.json.jsonGet
import eu.vendeli.rethis.shared.request.json.JsonGetOption
import eu.vendeli.rethis.shared.types.DataProcessingException
import eu.vendeli.rethis.types.interfaces.SerializationFormat
import eu.vendeli.rethis.utils.isInTx
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

suspend inline fun <reified T : Any> ReThis.jsonGet(
    key: String,
    vararg options: JsonGetOption,
): T? = jsonGet(key = key, options = options, serializer = serializer<T>())

suspend fun <T : Any> ReThis.jsonGet(
    key: String,
    vararg options: JsonGetOption,
    serializer: KSerializer<T>,
    format: SerializationFormat = cfg.serializationFormat,
): T? {
    if (isInTx()) {
        logger.warn(
            "Be aware that in transaction commands return `QUEUED`" +
                " which is for type safety substituted with default value, so serde operations will fail",
        )
    }
    val raw: String = jsonGet(key = key, options = options) ?: return null
    return try {
        format.deserialize(serializer, raw)
    } catch (ex: Exception) {
        throw DataProcessingException("Failed to deserialize jsonGet for key \"$key\"", ex)
    }
}
