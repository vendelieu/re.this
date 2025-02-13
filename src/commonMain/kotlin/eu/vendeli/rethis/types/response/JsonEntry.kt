package eu.vendeli.rethis.types.response

import eu.vendeli.rethis.types.common.VaryingArgument
import eu.vendeli.rethis.types.common.toArgument

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
