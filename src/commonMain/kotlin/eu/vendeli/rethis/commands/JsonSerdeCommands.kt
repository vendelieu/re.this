package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.types.common.toArgument
import eu.vendeli.rethis.types.options.UpsertMode
import eu.vendeli.rethis.utils.REDIS_JSON_ROOT_PATH
import eu.vendeli.rethis.utils.__jsonModule
import eu.vendeli.rethis.utils.execute
import eu.vendeli.rethis.utils.writeArgument
import io.ktor.util.reflect.*

suspend inline fun <reified T : Any> ReThis.jsonGet(key: String, path: String? = null): T? = execute(
    mutableListOf("JSON.GET".toArgument(), key.toArgument()).writeArgument(path),
    responseType = typeInfo<T>(),
    jsonModule = __jsonModule(),
)

suspend inline fun <reified T : Any> ReThis.jsonMGet(path: String, vararg key: String): List<T?> = execute(
    payload = listOf("JSON.MGET".toArgument(), *key.toArgument(), path.toArgument()),
    isCollectionResponse = true,
    responseType = typeInfo<T>(),
    jsonModule = __jsonModule(),
) ?: emptyList()

suspend inline fun <reified T : Any> ReThis.jsonSet(
    key: String,
    value: T,
    path: String = REDIS_JSON_ROOT_PATH,
    upsertMode: UpsertMode? = null,
): String? = execute<String>(
    mutableListOf(
        "JSON.SET".toArgument(),
        key.toArgument(),
        path.toArgument(),
        __jsonModule().encodeToString(value).toArgument(),
    ).writeArgument(upsertMode),
)
