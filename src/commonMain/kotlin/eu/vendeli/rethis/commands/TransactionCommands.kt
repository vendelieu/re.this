package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.types.core.RType
import eu.vendeli.rethis.types.core.toArg
import eu.vendeli.rethis.types.core.unwrapList

suspend fun ReThis.discard(): Boolean = execute<String>(
    listOf(
        "DISCARD".toArg(),
    ),
) == "OK"

suspend fun ReThis.exec(): List<RType> = execute(
    listOf(
        "EXEC".toArg(),
    ),
).unwrapList<RType>()

suspend fun ReThis.multi(): Boolean = execute<String>(
    listOf(
        "MULTI".toArg(),
    ),
) == "OK"

suspend fun ReThis.unwatch(): Boolean = execute<String>(
    listOf(
        "UNWATCH".toArg(),
    ),
) == "OK"

suspend fun ReThis.watch(vararg key: String): Boolean = execute<String>(
    listOf(
        "WATCH".toArg(),
        *key.toArg(),
    ),
) == "OK"
