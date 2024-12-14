package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.types.core.RType
import eu.vendeli.rethis.types.core.toArg
import eu.vendeli.rethis.types.core.unwrapList
import eu.vendeli.rethis.types.options.UpsertMode
import eu.vendeli.rethis.utils.writeArg

suspend fun ReThis.jsonArrAppend(key: String, path: String, vararg values: String): Long? = execute<Long>(
    listOf("JSON.ARRAPPEND".toArg(), key.toArg(), path.toArg(), *values.toArg()),
)

suspend fun ReThis.jsonArrIndex(key: String, path: String, value: String): Long? = execute<Long>(
    listOf("JSON.ARRINDEX".toArg(), key.toArg(), path.toArg(), value.toArg()),
)

suspend fun ReThis.jsonArrInsert(key: String, path: String, index: Long, vararg values: String): Long? = execute<Long>(
    listOf("JSON.ARRINSERT".toArg(), key.toArg(), path.toArg(), index.toArg(), *values.toArg()),
)

suspend fun ReThis.jsonArrLen(key: String, path: String): Long? = execute<Long>(
    listOf("JSON.ARRLEN".toArg(), key.toArg(), path.toArg()),
)

suspend fun ReThis.jsonArrPop(key: String, path: String, index: Long? = null): String? = execute<String>(
    mutableListOf("JSON.ARRPOP".toArg(), key.toArg(), path.toArg()).writeArg(index),
)

suspend fun ReThis.jsonArrTrim(key: String, path: String, start: Long, stop: Long): Long? = execute<Long>(
    listOf("JSON.ARRTRIM".toArg(), key.toArg(), path.toArg(), start.toArg(), stop.toArg()),
)

suspend fun ReThis.jsonClear(key: String): Long? = execute<Long>(
    listOf("JSON.CLEAR".toArg(), key.toArg()),
)

suspend fun ReThis.jsonDel(key: String, path: String): Long? = execute<Long>(
    listOf("JSON.DEL".toArg(), key.toArg(), path.toArg()),
)

suspend fun ReThis.jsonForget(key: String): Long? = execute<Long>(
    listOf("JSON.FORGET".toArg(), key.toArg()),
)

suspend fun ReThis.jsonGet(key: String, path: String? = null): String? = execute<String>(
    mutableListOf("JSON.GET".toArg(), key.toArg()).writeArg(path),
)

suspend fun ReThis.jsonMerge(key: String, path: String, value: String): String? = execute<String>(
    listOf("JSON.MERGE".toArg(), key.toArg(), path.toArg(), value.toArg()),
)

suspend fun ReThis.jsonMGet(key: String, vararg paths: String): List<String?> = execute(
    listOf("JSON.MGET".toArg(), key.toArg(), *paths.toArg()),
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.jsonMSet(key: String, path: String, value: String): String? = execute<String>(
    listOf("JSON.MSET".toArg(), key.toArg(), path.toArg(), value.toArg()),
)

suspend fun ReThis.jsonNumIncrBy(key: String, path: String, increment: Long): List<Long?> = execute(
    listOf("JSON.NUMINCRBY".toArg(), key.toArg(), path.toArg(), increment.toArg()),
).unwrapList<Long?>()

suspend fun ReThis.jsonNumMultBy(key: String, path: String, multiplier: Long): List<Long?> = execute(
    listOf("JSON.NUMMULTBY".toArg(), key.toArg(), path.toArg(), multiplier.toArg()),
).unwrapList()

suspend fun ReThis.jsonObjKeys(key: String, path: String): List<String> = execute(
    listOf("JSON.OBJKEYS".toArg(), key.toArg(), path.toArg()),
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.jsonObjLen(key: String, path: String): List<Long?> = execute(
    listOf("JSON.OBJLEN".toArg(), key.toArg(), path.toArg()),
).unwrapList<Long?>()

suspend fun ReThis.jsonResp(key: String, path: String? = null): List<RType> = execute(
    mutableListOf("JSON.RESP".toArg(), key.toArg()).writeArg(path),
).unwrapList()

suspend fun ReThis.jsonSet(
    key: String,
    path: String,
    value: String,
    upsertMode: UpsertMode? = null,
): String? = execute<String>(
    mutableListOf("JSON.SET".toArg(), key.toArg(), path.toArg(), value.toArg()).writeArg(upsertMode),
)

suspend fun ReThis.jsonStrAppend(key: String, value: String, path: String? = null): Long? = execute<Long>(
    listOfNotNull("JSON.STRAPPEND".toArg(), key.toArg(), path?.toArg(), value.toArg()),
)

suspend fun ReThis.jsonStrLen(key: String, path: String): Long? = execute<Long>(
    listOf("JSON.STRLEN".toArg(), key.toArg(), path.toArg()),
)

suspend fun ReThis.jsonToggle(key: String, path: String): List<Long> = execute(
    listOf("JSON.TOGGLE".toArg(), key.toArg(), path.toArg()),
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.jsonType(key: String, path: String): List<String> = execute(
    listOf("JSON.TYPE".toArg(), key.toArg(), path.toArg()),
    isCollectionResponse = true,
) ?: emptyList()
