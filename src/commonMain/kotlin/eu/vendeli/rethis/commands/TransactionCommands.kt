package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.types.core.RType
import eu.vendeli.rethis.types.core.toArg
import eu.vendeli.rethis.types.core.unwrapList

suspend fun ReThis.discard(): String? = execute<String>(
    listOf(
        "DISCARD".toArg(),
    ),
)

suspend fun ReThis.exec(): List<RType> = execute(
    listOf(
        "EXEC".toArg(),
    ),
).unwrapList<RType>()

suspend fun ReThis.multi(): String? = execute<String>(
    listOf(
        "MULTI".toArg(),
    ),
)

suspend fun ReThis.unwatch(): String? = execute<String>(
    listOf(
        "UNWATCH".toArg(),
    ),
)

suspend fun ReThis.watch(vararg key: String): String? = execute<String>(
    listOf(
        "WATCH".toArg(),
        *key.toArg(),
    ),
)
