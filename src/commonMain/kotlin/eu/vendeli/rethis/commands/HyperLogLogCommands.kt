package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.types.core.RType
import eu.vendeli.rethis.types.core.unwrap

suspend fun ReThis.pfAdd(key: String, vararg element: String): Boolean = execute(
    listOf(
        "PFADD",
        key,
        *element,
    ),
).unwrap<Long>() == 1L

suspend fun ReThis.pfCount(vararg key: String): Long = execute(
    listOf(
        "PFCOUNT",
        *key,
    ),
).unwrap() ?: 0

suspend fun ReThis.pfMerge(destset: String, vararg sourcekey: String): String? = execute(
    listOf(
        "PFMERGE",
        destset,
        *sourcekey,
    ),
).unwrap()

suspend fun ReThis.pfSelfTest(): RType? = execute(
    listOf(
        "PFSELFTEST",
    ),
).unwrap()
