package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.types.core.RType
import eu.vendeli.rethis.types.core.toArg
import eu.vendeli.rethis.types.core.unwrap
import eu.vendeli.rethis.types.core.unwrapList

suspend fun ReThis.discard(): String? = execute(
    listOf(
        "DISCARD".toArg(),
    ),
).unwrap()

suspend fun ReThis.exec(): List<RType> = execute(
    listOf(
        "EXEC".toArg(),
    ),
).unwrapList<RType>()

suspend fun ReThis.multi(): String? = execute(
    listOf(
        "MULTI".toArg(),
    ),
).unwrap()

suspend fun ReThis.unwatch(): String? = execute(
    listOf(
        "UNWATCH".toArg(),
    ),
).unwrap()

suspend fun ReThis.watch(vararg key: String): String? = execute(
    listOf(
        "WATCH".toArg(),
        *key.toArg(),
    ),
).unwrap()
