package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.types.core.RType
import eu.vendeli.rethis.types.core.toArg
import eu.vendeli.rethis.utils.unwrapRespIndMap
import eu.vendeli.rethis.utils.writeArg
import io.ktor.utils.io.core.*

suspend fun ReThis.hello(
    proto: Int = 3,
    username: String? = null,
    password: String? = null,
    name: String? = null,
): Map<String, RType?>? = execute(
    mutableListOf("HELLO".toArg()).apply {
        writeArg(proto)
        if (username != null && password != null) {
            writeArg("AUTH")
            writeArg(username)
            writeArg(password)
        }
        name?.also {
            writeArg("SETNAME")
            writeArg(it)
        }
    },
).unwrapRespIndMap()

suspend fun ReThis.ping(message: String? = null): String? = execute<String>(
    mutableListOf(
        "PING".toArg(),
    ).writeArg(message),
)

suspend fun ReThis.select(database: Int): String? = execute<String>(
    listOf(
        "SELECT".toArg(),
        database.toArg(),
    ),
)
