package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.types.common.toArgument
import eu.vendeli.rethis.utils.writeArgument
import eu.vendeli.rethis.utils.execute
import eu.vendeli.rethis.utils.unwrapRESPAgnosticMap

suspend fun ReThis.hello(
    proto: Int = 3,
    username: String? = null,
    password: String? = null,
    name: String? = null,
): Map<String, RType?>? = execute(
    mutableListOf("HELLO".toArgument()).apply {
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
).unwrapRESPAgnosticMap()

suspend fun ReThis.ping(message: String? = null): String? = execute<String>(
    mutableListOf(
        "PING".toArgument(),
    ).writeArgument(message),
)

suspend fun ReThis.select(database: Int): Boolean = execute<String>(
    listOf(
        "SELECT".toArgument(),
        database.toArgument(),
    ),
) == "OK"
