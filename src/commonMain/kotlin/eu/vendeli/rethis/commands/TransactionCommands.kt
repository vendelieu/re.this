package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.types.common.RType
import eu.vendeli.rethis.types.common.toArgument
import eu.vendeli.rethis.utils.execute
import eu.vendeli.rethis.utils.unwrapList

suspend fun ReThis.discard(): Boolean = execute<String>(
    listOf(
        "DISCARD".toArgument(),
    ),
) == "OK"

suspend fun ReThis.exec(): List<RType> = execute(
    listOf(
        "EXEC".toArgument(),
    ),
).unwrapList<RType>()

suspend fun ReThis.multi(): Boolean = execute<String>(
    listOf(
        "MULTI".toArgument(),
    ),
) == "OK"

suspend fun ReThis.unwatch(): Boolean = execute<String>(
    listOf(
        "UNWATCH".toArgument(),
    ),
) == "OK"

suspend fun ReThis.watch(vararg key: String): Boolean = execute<String>(
    listOf(
        "WATCH".toArgument(),
        *key.toArgument(),
    ),
) == "OK"
