package eu.vendeli.rethis.command.serde

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.command.string.set
import eu.vendeli.rethis.shared.request.string.SetOption
import eu.vendeli.rethis.types.interfaces.SerializationFormat
import eu.vendeli.rethis.utils.isInTx
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

suspend inline fun <reified T> ReThis.`set`(
    key: String,
    value: T,
    vararg options: SetOption,
): String? where T : Any {
    return set(key, value, serializer<T>(), *options)
}

suspend fun <T> ReThis.`set`(
    key: String,
    value: T,
    serializer: KSerializer<T>,
    vararg options: SetOption,
    serializationFormat: SerializationFormat = cfg.serializationFormat,
): String? {
    if (isInTx()) {
        logger.warn("Be aware that in transaction commands return `QUEUED`" +
            " which is for type safety substituted with default value, so serde operations will fail")
    }
    val value = serializationFormat.serialize(serializer, value)
    return set(key, value, *options)
}
