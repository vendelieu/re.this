package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.types.core.RType
import eu.vendeli.rethis.types.core.unwrap
import eu.vendeli.rethis.types.core.unwrapList
import eu.vendeli.rethis.types.options.UpsertMode

suspend fun ReThis.jsonArrAppend(key: String, path: String, vararg values: String): Long? = execute(
    listOf("JSON.ARRAPPEND", key, path, *values),
).unwrap()

suspend fun ReThis.jsonArrIndex(key: String, path: String, value: String): Long? = execute(
    listOf("JSON.ARRINDEX", key, path, value),
).unwrap()

suspend fun ReThis.jsonArrInsert(key: String, path: String, index: Long, vararg values: String): Long? = execute(
    listOf("JSON.ARRINSERT", key, path, index, *values),
).unwrap()

suspend fun ReThis.jsonArrLen(key: String, path: String): Long? = execute(
    listOf("JSON.ARRLEN", key, path),
).unwrap()

suspend fun ReThis.jsonArrPop(key: String, path: String, index: Long? = null): String? = execute(
    listOfNotNull("JSON.ARRPOP", key, path, index),
).unwrap()

suspend fun ReThis.jsonArrTrim(key: String, path: String, start: Long, stop: Long): Long? = execute(
    listOf("JSON.ARRTRIM", key, path, start, stop),
).unwrap()

suspend fun ReThis.jsonClear(key: String): Long? = execute(
    listOf("JSON.CLEAR", key),
).unwrap()

suspend fun ReThis.jsonDel(key: String, path: String): Long? = execute(
    listOf("JSON.DEL", key, path),
).unwrap()

suspend fun ReThis.jsonForget(key: String): Long? = execute(
    listOf("JSON.FORGET", key),
).unwrap()

suspend fun ReThis.jsonGet(key: String, path: String? = null): String? = execute(
    listOf("JSON.GET", key) + (path?.let { listOf(it) } ?: emptyList()),
).unwrap()

suspend fun ReThis.jsonMerge(key: String, path: String, value: String): String? = execute(
    listOf("JSON.MERGE", key, path, value),
).unwrap()

suspend fun ReThis.jsonMGet(key: String, vararg paths: String): List<String?> = execute(
    listOf("JSON.MGET", key, *paths),
).unwrapList()

suspend fun ReThis.jsonMSet(key: String, path: String, value: String): String? = execute(
    listOf("JSON.MSET", key, path, value),
).unwrap()

suspend fun ReThis.jsonNumIncrBy(key: String, path: String, increment: Long): List<Long?> = execute(
    listOf("JSON.NUMINCRBY", key, path, increment),
).unwrapList<Long?>()

suspend fun ReThis.jsonNumMultBy(key: String, path: String, multiplier: Long): List<Long?> = execute(
    listOf("JSON.NUMMULTBY", key, path, multiplier),
).unwrapList()

suspend fun ReThis.jsonObjKeys(key: String, path: String): List<String> = execute(
    listOf("JSON.OBJKEYS", key, path),
).unwrapList()

suspend fun ReThis.jsonObjLen(key: String, path: String): List<Long?> = execute(
    listOf("JSON.OBJLEN", key, path),
).unwrapList<Long?>()

suspend fun ReThis.jsonResp(key: String, path: String? = null): List<RType> = execute(
    listOfNotNull("JSON.RESP", key, path),
).unwrapList()

suspend fun ReThis.jsonSet(
    key: String,
    path: String,
    value: String,
    upsertMode: UpsertMode? = null,
): String? = execute(
    listOfNotNull("JSON.SET", key, path, value, upsertMode),
).unwrap()

suspend fun ReThis.jsonStrAppend(key: String, value: String, path: String? = null): Long? = execute(
    listOfNotNull("JSON.STRAPPEND", key, path, value),
).unwrap()

suspend fun ReThis.jsonStrLen(key: String, path: String): Long? = execute(
    listOf("JSON.STRLEN", key, path),
).unwrap()

suspend fun ReThis.jsonToggle(key: String, path: String): List<Long> = execute(
    listOf("JSON.TOGGLE", key, path),
).unwrapList()

suspend fun ReThis.jsonType(key: String, path: String): List<String> = execute(
    listOf("JSON.TYPE", key, path),
).unwrapList()
