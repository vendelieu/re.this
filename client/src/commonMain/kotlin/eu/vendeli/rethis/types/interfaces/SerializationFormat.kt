package eu.vendeli.rethis.types.interfaces

import kotlinx.serialization.KSerializer

interface SerializationFormat {
    fun <T> serialize(serializer: KSerializer<T>, value: T): String
    fun <T> deserialize(serializer: KSerializer<T>, string: String): T
}
