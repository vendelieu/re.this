package eu.vendeli.rethis.command.serde

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.command.hash.hMSet
import eu.vendeli.rethis.shared.request.common.FieldValue
import eu.vendeli.rethis.types.interfaces.SerializationFormat
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

suspend inline fun <reified T : Any> ReThis.hMSet(
    key: String,
    vararg fieldValue: Pair<String, T>,
): Boolean = hMSet(key, fieldValue = fieldValue, serializer<T>())

suspend fun <T : Any> ReThis.hMSet(
    key: String,
    vararg fieldValue: Pair<String, T>,
    serializer: KSerializer<T>,
    format: SerializationFormat = cfg.serializationFormat,
): Boolean {
    val serializedPairs = fieldValue.map { (f, v) ->
        FieldValue(f, format.serialize(serializer, v))
    }.toTypedArray()
    return hMSet(key = key, data = serializedPairs)
}
