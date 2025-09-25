package eu.vendeli.rethis.command.serde

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.command.string.mSet
import eu.vendeli.rethis.shared.request.string.KeyValue
import eu.vendeli.rethis.types.interfaces.SerializationFormat
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

suspend inline fun <reified T : Any> ReThis.mSet(
    vararg kvPair: Pair<String, T>,
): Boolean = mSet(kvPair = kvPair, serializer = serializer<T>())

suspend fun <T : Any> ReThis.mSet(
    vararg kvPair: Pair<String, T>,
    serializer: KSerializer<T>,
    format: SerializationFormat = cfg.serializationFormat,
): Boolean {
    val serializedPairs = kvPair.map { (k, v) ->
        KeyValue(k, format.serialize(serializer, v))
    }.toTypedArray()
    return mSet(*serializedPairs)
}
