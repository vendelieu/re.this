package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.types.core.toArgument
import eu.vendeli.rethis.utils.__jsonModule
import eu.vendeli.rethis.utils.execute
import eu.vendeli.rethis.utils.writeArgument
import io.ktor.util.reflect.typeInfo

suspend inline fun <reified T> ReThis.hSet(key: String, vararg fieldValue: Pair<String, T>): Long? = execute<Long>(
    mutableListOf(
        "HSET".toArgument(),
        key.toArgument(),
    ).apply {
        fieldValue.forEach {
            writeArgument(it.first)
            writeArgument(__jsonModule().encodeToString(it.second))
        }
    },
)

suspend inline fun <reified T : Any> ReThis.hGet(key: String, field: String): T? = execute(
    payload = listOf(
        "HGET".toArgument(),
        key.toArgument(),
        field.toArgument(),
    ),
    responseType = typeInfo<T>(),
    jsonModule = __jsonModule(),
)

suspend inline fun <reified T : Any> ReThis.hMGet(key: String, vararg field: String): List<T?> = execute(
    payload = listOf(
        "HMGET".toArgument(),
        key.toArgument(),
        *field.toArgument(),
    ),
    isCollectionResponse = true,
    responseType = typeInfo<T>(),
    jsonModule = __jsonModule(),
) ?: emptyList()

suspend inline fun <reified T> ReThis.hMSet(
    key: String,
    vararg fieldValue: Pair<String, T>,
): Boolean = execute<String>(
    mutableListOf(
        "HMSET".toArgument(),
        key.toArgument(),
    ).apply {
        fieldValue.forEach {
            writeArgument(it.first to __jsonModule().encodeToString(it.second))
        }
    },
) == "OK"

suspend inline fun <reified T : Any> ReThis.hVals(key: String): List<T> = execute(
    payload = listOf(
        "HVALS".toArgument(),
        key.toArgument(),
    ),
    isCollectionResponse = true,
    responseType = typeInfo<T>(),
    jsonModule = __jsonModule(),
) ?: emptyList()
