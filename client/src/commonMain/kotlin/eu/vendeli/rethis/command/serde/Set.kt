package eu.vendeli.rethis.command.serde

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.shared.request.string.SetOption
import eu.vendeli.rethis.types.interfaces.SerializationFormat
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

suspend inline fun <reified T> ReThis.`set`(
    key: String,
    value: T,
    vararg options: SetOption
): String? where T : Any {
    return set(key, value, serializer<T>(), *options)
}

suspend fun <T> ReThis.`set`(
    key: String,
    value: T,
    serializer: KSerializer<T>,
    vararg options: SetOption
): String? {
    val value = cfg.serializationFormat.serialize(serializer, value)
    return set(key, value, *options)
}

suspend fun <T> ReThis.`set`(
    key: String,
    value: T,
    serializer: KSerializer<T>,
    serializationFormat: SerializationFormat,
    vararg options: SetOption
): String? {
    val value = serializationFormat.serialize(serializer, value)
    return set(key, value, *options)
}
