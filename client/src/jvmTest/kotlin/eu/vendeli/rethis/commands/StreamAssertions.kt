package eu.vendeli.rethis.commands

import eu.vendeli.rethis.shared.types.BulkString
import eu.vendeli.rethis.shared.types.RArray
import eu.vendeli.rethis.shared.types.RMap
import eu.vendeli.rethis.shared.types.RType

/** A stream entry as the client returns it unparsed: `[id, [field, value, ...]]`. */
internal fun streamEntry(id: String, vararg fieldValues: String): RType =
    RArray(listOf(BulkString(id), RArray(fieldValues.map { BulkString(it) })))

/** Looks up [key] in a RESP3 map reply whose keys are bulk strings. */
internal fun RType.mapField(key: String): RType? = (this as RMap).value[BulkString(key)]
