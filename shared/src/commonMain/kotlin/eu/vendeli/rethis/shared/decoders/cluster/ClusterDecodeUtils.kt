package eu.vendeli.rethis.shared.decoders.cluster

import eu.vendeli.rethis.shared.types.Int64
import eu.vendeli.rethis.shared.types.PlainString
import eu.vendeli.rethis.shared.types.RArray
import eu.vendeli.rethis.shared.types.RMap
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.types.ResponseParsingException
import eu.vendeli.rethis.shared.utils.unwrap

internal fun RType.asArray(what: String): List<RType> =
    (this as? RArray)?.value ?: throw ResponseParsingException("Invalid $what: expected array, given $this")

internal fun RType.asLong(what: String): Long = when (this) {
    is Int64 -> value
    is PlainString -> value.toLongOrNull()
    else -> unwrap<String>()?.toLongOrNull()
} ?: throw ResponseParsingException("Invalid $what: expected integer, given $this")

/** Field access over both reply shapes: RESP2 flat key-value array and RESP3 map. */
internal fun RType.asFields(what: String): Map<String, RType?> = when (this) {
    is RMap -> value.entries.associate { (key, value) -> key.fieldName(what) to value }
    is RArray -> value.chunked(2).associate { it[0].fieldName(what) to it.getOrNull(1) }
    else -> throw ResponseParsingException("Invalid $what: expected map or array, given $this")
}

private fun RType.fieldName(what: String): String = unwrap<String>() ?: throw ResponseParsingException(
    "Invalid $what: field name must be a string, given $this",
)
