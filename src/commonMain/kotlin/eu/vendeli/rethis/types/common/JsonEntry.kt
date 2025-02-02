package eu.vendeli.rethis.types.common

import eu.vendeli.rethis.types.core.VaryingArgument
import eu.vendeli.rethis.types.core.toArgument

class JsonEntry(
    key: String,
    path: String,
    value: String,
) : VaryingArgument {
    override val data = listOf(
        key.toArgument(),
        path.toArgument(),
        value.toArgument(),
    )
}
