package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.types.common.toArgument
import eu.vendeli.rethis.types.options.GetExOption
import eu.vendeli.rethis.utils.__jsonModule
import eu.vendeli.rethis.utils.execute
import eu.vendeli.rethis.utils.writeArgument
import io.ktor.util.reflect.typeInfo

suspend inline fun <reified T> ReThis.mSet(vararg kvPair: Pair<String, T>): Boolean = execute<String>(
    mutableListOf(
        "MSET".toArgument(),
    ).apply {
        kvPair.forEach {
            writeArgument(it.first)
            writeArgument(__jsonModule().encodeToString(it.second))
        }
    },
) == "OK"

suspend inline fun <reified T : Any> ReThis.mGet(vararg key: String): List<T?> = execute(
    payload = listOf(
        "MGET".toArgument(),
        *key.toArgument(),
    ),
    isCollectionResponse = true,
    responseType = typeInfo<T>(),
    jsonModule = __jsonModule(),
) ?: emptyList()

suspend inline fun <reified T> ReThis.set(
    key: String,
    value: T,
): Boolean = execute<String>(
    listOf(
        "SET".toArgument(),
        key.toArgument(),
        __jsonModule().encodeToString(value).toArgument(),
    ),
) == "OK"

suspend inline fun <reified T : Any> ReThis.get(key: String): T? = execute(
    payload = listOf(
        "GET".toArgument(),
        key.toArgument(),
    ),
    responseType = typeInfo<T>(),
    jsonModule = __jsonModule(),
)

suspend inline fun <reified T : Any> ReThis.getDel(key: String): T? = execute(
    payload = listOf(
        "GETDEL".toArgument(),
        key.toArgument(),
    ),
    responseType = typeInfo<T>(),
    jsonModule = __jsonModule(),
)

suspend inline fun <reified T : Any> ReThis.getEx(
    key: String,
    option: GetExOption,
): T? = execute(
    payload = mutableListOf(
        "GETEX".toArgument(),
        key.toArgument(),
    ).writeArgument(option),
    responseType = typeInfo<T>(),
    jsonModule = __jsonModule(),
)

suspend inline fun <reified T : Any> ReThis.lcs(key1: String, key2: String): T? = execute(
    payload = listOf(
        "LCS".toArgument(),
        key1.toArgument(),
        key2.toArgument(),
    ),
    responseType = typeInfo<T>(),
    jsonModule = __jsonModule(),
)
