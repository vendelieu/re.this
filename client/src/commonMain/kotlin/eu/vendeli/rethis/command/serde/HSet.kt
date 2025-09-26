package eu.vendeli.rethis.command.serde

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.command.hash.hSet
import eu.vendeli.rethis.shared.request.common.FieldValue
import eu.vendeli.rethis.types.interfaces.SerializationFormat
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

suspend inline fun <reified T : Any> ReThis.hSet(
    key: String,
    vararg fieldValue: Pair<String, T>,
): Long = hSet(key, fieldValue = fieldValue, serializer<T>())

suspend fun <T : Any> ReThis.hSet(
    key: String,
    vararg fieldValue: Pair<String, T>,
    serializer: KSerializer<T>,
    format: SerializationFormat = cfg.serializationFormat,
): Long {
    val serializedPairs = fieldValue.map { (f, v) ->
        FieldValue(f, format.serialize(serializer, v))
    }.toTypedArray()
    return hSet(key = key, data = serializedPairs)
}


