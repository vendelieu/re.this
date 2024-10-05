package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.types.core.RType
import eu.vendeli.rethis.types.core.toArg
import eu.vendeli.rethis.types.core.unwrap

suspend fun ReThis.pfAdd(key: String, vararg element: String): Boolean = execute(
    listOf(
        "PFADD".toArg(),
        key.toArg(),
        *element.toArg(),
    ),
).unwrap<Long>() == 1L

suspend fun ReThis.pfCount(vararg key: String): Long = execute(
    listOf(
        "PFCOUNT".toArg(),
        *key.toArg(),
    ),
).unwrap() ?: 0

suspend fun ReThis.pfMerge(destset: String, vararg sourcekey: String): String? = execute(
    listOf(
        "PFMERGE".toArg(),
        destset.toArg(),
        *sourcekey.toArg(),
    ),
).unwrap()

suspend fun ReThis.pfSelfTest(): RType? = execute(
    listOf(
        "PFSELFTEST".toArg(),
    ),
).unwrap()
