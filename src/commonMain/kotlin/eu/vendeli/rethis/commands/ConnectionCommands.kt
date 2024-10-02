package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.types.core.RType
import eu.vendeli.rethis.types.core.unwrap
import eu.vendeli.rethis.utils.unwrapRespIndMap

suspend fun ReThis.hello(
    proto: Int = 3,
    username: String? = null,
    password: String? = null,
    name: String? = null,
): Map<String, RType?>? = execute(
    listOfNotNull(
        "HELLO",
        proto,
        if (username != null && password != null) "AUTH" else null,
        username,
        password,
        name?.let { "SETNAME" to it },
    ),
).unwrapRespIndMap()

suspend fun ReThis.ping(message: String? = null): String? = execute(
    listOfNotNull(
        "PING",
        message,
    ),
).unwrap()

suspend fun ReThis.select(database: Int): String? = execute(
    listOf(
        "SELECT",
        database,
    ),
).unwrap()
