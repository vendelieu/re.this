package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.types.response.JsonEntry
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.types.common.toArgument
import eu.vendeli.rethis.types.options.UpsertMode
import eu.vendeli.rethis.utils.REDIS_JSON_ROOT_PATH
import eu.vendeli.rethis.utils.execute
import eu.vendeli.rethis.utils.unwrapList
import eu.vendeli.rethis.utils.writeArgument

suspend fun ReThis.jsonArrAppend(key: String, path: String, vararg values: String): Long? = execute<Long>(
    listOf("JSON.ARRAPPEND".toArgument(), key.toArgument(), path.toArgument(), *values.toArgument()),
)

suspend fun ReThis.jsonArrIndex(key: String, path: String, value: String): Long? = execute<Long>(
    listOf("JSON.ARRINDEX".toArgument(), key.toArgument(), path.toArgument(), value.toArgument()),
)

suspend fun ReThis.jsonArrInsert(key: String, path: String, index: Long, vararg values: String): Long? = execute<Long>(
    listOf(
        "JSON.ARRINSERT".toArgument(),
        key.toArgument(),
        path.toArgument(),
        index.toArgument(),
        *values.toArgument(),
    ),
)

suspend fun ReThis.jsonArrLen(key: String, path: String): Long? = execute<Long>(
    listOf("JSON.ARRLEN".toArgument(), key.toArgument(), path.toArgument()),
)

suspend fun ReThis.jsonArrPop(key: String, path: String, index: Long? = null): RType? = execute(
    mutableListOf("JSON.ARRPOP".toArgument(), key.toArgument(), path.toArgument()).writeArgument(index),
)

suspend fun ReThis.jsonArrTrim(key: String, path: String, start: Long, stop: Long): Long? = execute<Long>(
    listOf("JSON.ARRTRIM".toArgument(), key.toArgument(), path.toArgument(), start.toArgument(), stop.toArgument()),
)

suspend fun ReThis.jsonClear(key: String): Long? = execute<Long>(
    listOf("JSON.CLEAR".toArgument(), key.toArgument()),
)

suspend fun ReThis.jsonDel(key: String, path: String): Long? = execute<Long>(
    listOf("JSON.DEL".toArgument(), key.toArgument(), path.toArgument()),
)

suspend fun ReThis.jsonForget(key: String): Long? = execute<Long>(
    listOf("JSON.FORGET".toArgument(), key.toArgument()),
)

suspend fun ReThis.jsonGet(key: String, path: String? = null): String? = execute<String>(
    mutableListOf("JSON.GET".toArgument(), key.toArgument()).writeArgument(path),
)

suspend fun ReThis.jsonMerge(key: String, path: String, value: String): Boolean = execute<String>(
    listOf("JSON.MERGE".toArgument(), key.toArgument(), path.toArgument(), value.toArgument()),
) == "OK"

suspend fun ReThis.jsonMGet(path: String, vararg key: String): List<String?> = execute(
    listOf("JSON.MGET".toArgument(), *key.toArgument(), path.toArgument()),
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.jsonMSet(vararg entry: JsonEntry): Boolean = execute<String>(
    mutableListOf("JSON.MSET".toArgument()).writeArgument(entry),
) == "OK"

suspend fun ReThis.jsonNumIncrBy(key: String, path: String, increment: Long): List<Long?> = execute<Long>(
    listOf("JSON.NUMINCRBY".toArgument(), key.toArgument(), path.toArgument(), increment.toArgument()),
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.jsonNumMultBy(key: String, path: String, multiplier: Long): List<Long?> = execute(
    listOf("JSON.NUMMULTBY".toArgument(), key.toArgument(), path.toArgument(), multiplier.toArgument()),
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.jsonObjKeys(key: String, path: String): List<String> = execute(
    listOf("JSON.OBJKEYS".toArgument(), key.toArgument(), path.toArgument()),
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.jsonObjLen(key: String, path: String): List<Long?> = execute<Long>(
    listOf("JSON.OBJLEN".toArgument(), key.toArgument(), path.toArgument()),
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.jsonResp(key: String, path: String? = null): List<RType> = execute(
    mutableListOf("JSON.RESP".toArgument(), key.toArgument()).writeArgument(path),
).unwrapList()

suspend fun ReThis.jsonSet(
    key: String,
    value: String,
    path: String = REDIS_JSON_ROOT_PATH,
    upsertMode: UpsertMode? = null,
): String? = execute<String>(
    mutableListOf("JSON.SET".toArgument(), key.toArgument(), path.toArgument(), value.toArgument()).writeArgument(
        upsertMode,
    ),
)

suspend fun ReThis.jsonStrAppend(key: String, value: String, path: String? = null): Long? = execute<Long>(
    listOfNotNull("JSON.STRAPPEND".toArgument(), key.toArgument(), path?.toArgument(), value.toArgument()),
)

suspend fun ReThis.jsonStrLen(key: String, path: String): Long? = execute<Long>(
    listOf("JSON.STRLEN".toArgument(), key.toArgument(), path.toArgument()),
)

suspend fun ReThis.jsonToggle(key: String, path: String): List<Long> = execute(
    listOf("JSON.TOGGLE".toArgument(), key.toArgument(), path.toArgument()),
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.jsonType(key: String, path: String): List<String> = execute(
    listOf("JSON.TYPE".toArgument(), key.toArgument(), path.toArgument()),
    isCollectionResponse = true,
) ?: emptyList()
