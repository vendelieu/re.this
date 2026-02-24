package eu.vendeli.rethis.command.serde

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.command.json.jsonSet
import eu.vendeli.rethis.shared.request.string.UpsertMode
import eu.vendeli.rethis.types.interfaces.SerializationFormat
import eu.vendeli.rethis.utils.JSON_DEFAULT_PATH
import eu.vendeli.rethis.utils.isInTx
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

suspend inline fun <reified T : Any> ReThis.jsonSet(
    key: String,
    value: T,
    path: String = JSON_DEFAULT_PATH,
    upsertMode: UpsertMode? = null,
): String = jsonSet(key, value, serializer<T>(), path, upsertMode)

suspend fun <T : Any> ReThis.jsonSet(
    key: String,
    value: T,
    serializer: KSerializer<T>,
    path: String = JSON_DEFAULT_PATH,
    upsertMode: UpsertMode? = null,
    format: SerializationFormat = cfg.serializationFormat,
): String {
    if (isInTx()) {
        logger.warn(
            "Be aware that in transaction commands return `QUEUED`" +
                " which is for type safety substituted with default value, so serde operations will fail",
        )
    }
    val serialized = format.serialize(serializer, value)
    return jsonSet(key, serialized, path, upsertMode)
}
