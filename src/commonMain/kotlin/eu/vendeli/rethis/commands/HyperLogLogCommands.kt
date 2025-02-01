package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.types.core.RType
import eu.vendeli.rethis.types.core.toArgument
import eu.vendeli.rethis.types.core.unwrap
import kotlin.Long
import eu.vendeli.rethis.utils.execute

suspend fun ReThis.pfAdd(key: String, vararg element: String): Boolean = execute<Long>(
    listOf(
        "PFADD".toArgument(),
        key.toArgument(),
        *element.toArgument(),
    ),
) == 1L

suspend fun ReThis.pfCount(vararg key: String): Long = execute<Long>(
    listOf(
        "PFCOUNT".toArgument(),
        *key.toArgument(),
    ),
) ?: 0

suspend fun ReThis.pfMerge(destset: String, vararg sourcekey: String): String? = execute<String>(
    listOf(
        "PFMERGE".toArgument(),
        destset.toArgument(),
        *sourcekey.toArgument(),
    ),
)

suspend fun ReThis.pfSelfTest(): RType? = execute(
    listOf(
        "PFSELFTEST".toArgument(),
    ),
).unwrap()
