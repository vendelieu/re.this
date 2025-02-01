package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.types.core.RType
import eu.vendeli.rethis.types.core.toArg
import eu.vendeli.rethis.utils.response.unwrapRespIndMap
import eu.vendeli.rethis.utils.writeArgument
import eu.vendeli.rethis.utils.execute

suspend fun ReThis.hello(
    proto: Int = 3,
    username: String? = null,
    password: String? = null,
    name: String? = null,
): Map<String, RType?>? = execute(
    mutableListOf("HELLO".toArg()).apply {
        writeArgument(proto)
        if (username != null && password != null) {
            writeArgument("AUTH")
            writeArgument(username)
            writeArgument(password)
        }
        name?.also {
            writeArgument("SETNAME")
            writeArgument(it)
        }
    },
).unwrapRespIndMap()

suspend fun ReThis.ping(message: String? = null): String? = execute<String>(
    mutableListOf(
        "PING".toArg(),
    ).writeArgument(message),
)

suspend fun ReThis.select(database: Int): Boolean = execute<String>(
    listOf(
        "SELECT".toArg(),
        database.toArg(),
    ),
) == "OK"
