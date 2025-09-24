package eu.vendeli.rethis.core

import eu.vendeli.rethis.types.interfaces.SerializationFormat
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

class JsonSerializationFormat(
  private val json: Json = Json { encodeDefaults = true }
): SerializationFormat {
  override fun <T> serialize(serializer: KSerializer<T>, value: T): String =
    json.encodeToString(serializer, value)

  override fun <T> deserialize(serializer: KSerializer<T>, string: String): T =
    json.decodeFromString(serializer, string)
}
