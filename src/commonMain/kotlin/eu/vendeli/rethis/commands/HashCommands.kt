package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.exception
import eu.vendeli.rethis.types.common.ScanResult
import eu.vendeli.rethis.types.core.RArray
import eu.vendeli.rethis.types.core.unwrap
import eu.vendeli.rethis.types.core.unwrapList
import eu.vendeli.rethis.types.options.HScanOption
import eu.vendeli.rethis.types.options.UpdateStrategyOption
import eu.vendeli.rethis.utils.safeCast
import eu.vendeli.rethis.utils.unwrapRespIndMap
import kotlinx.datetime.Instant
import kotlin.time.Duration

suspend fun ReThis.hDel(key: String, vararg field: String): Long = execute(
    listOf(
        "HDEL",
        key,
        *field,
    ),
).unwrap() ?: 0

suspend fun ReThis.hExists(key: String, field: String): Boolean = execute(
    listOf(
        "HEXISTS",
        key,
        field,
    ),
).unwrap<Long>() == 1L

suspend fun ReThis.hExpire(
    key: String,
    seconds: Duration,
    vararg field: String,
    updateType: UpdateStrategyOption? = null,
): List<Long> = execute(
    listOfNotNull(
        "HEXPIRE",
        key,
        seconds.inWholeSeconds,
        updateType,
        "FIELDS",
        field.size,
        *field,
    ),
).unwrapList()

suspend fun ReThis.hExpireAt(
    key: String,
    instant: Instant,
    vararg field: String,
    updateType: UpdateStrategyOption? = null,
): List<Long> = execute(
    listOfNotNull(
        "HEXPIREAT",
        key,
        instant.epochSeconds,
        updateType,
        "FIELDS",
        field.size,
        *field,
    ),
).unwrapList()

suspend fun ReThis.hExpireTime(
    key: String,
    vararg field: String,
): List<Long> = execute(
    listOf(
        "HEXPIRETIME",
        key,
        "FIELDS",
        field.size,
        *field,
    ),
).unwrapList()

suspend fun ReThis.hGet(key: String, field: String): String? = execute(
    listOf(
        "HGET",
        key,
        field,
    ),
).unwrap()

suspend fun ReThis.hGetAll(key: String): Map<String, String?>? = execute(
    listOf(
        "HGETALL",
        key,
    ),
).unwrapRespIndMap()

suspend fun ReThis.hIncrBy(key: String, field: String, increment: Long): Long = execute(
    listOf(
        "HINCRBY",
        key,
        field,
        increment,
    ),
).unwrap() ?: 0

suspend fun ReThis.hIncrByFloat(key: String, field: String, increment: Double): Double? = execute(
    listOf(
        "HINCRBYFLOAT",
        key,
        field,
        increment,
    ),
).unwrap<String>()?.toDoubleOrNull()

suspend fun ReThis.hKeys(key: String): List<String> = execute(
    listOf(
        "HKEYS",
        key,
    ),
).unwrapList<String>()

suspend fun ReThis.hLen(key: String): Long = execute(
    listOf(
        "HLEN",
        key,
    ),
).unwrap() ?: 0

suspend fun ReThis.hMGet(key: String, vararg field: String): List<String> = execute(
    listOf(
        "HMGET",
        key,
        *field,
    ),
).unwrapList()

suspend fun ReThis.hMSet(key: String, vararg fieldValue: Pair<String, String>): String? = execute(
    listOf(
        "HMSET",
        key,
        *fieldValue,
    ),
).unwrap()

suspend fun ReThis.hPersist(
    key: String,
    vararg field: String,
): List<Long> = execute(
    listOf(
        "HPERSIST",
        key,
        "FIELDS",
        field.size,
        *field,
    ),
).unwrapList()

suspend fun ReThis.hPExpire(
    key: String,
    milliseconds: Duration,
    vararg field: String,
    updateType: UpdateStrategyOption? = null,
): List<Long> = execute(
    listOfNotNull(
        "HPEXPIRE",
        key,
        milliseconds.inWholeMilliseconds,
        updateType,
        "FIELDS",
        field.size,
        *field,
    ),
).unwrapList()

suspend fun ReThis.hPExpireAt(
    key: String,
    instant: Instant,
    vararg field: String,
    updateType: UpdateStrategyOption? = null,
): List<Long> = execute(
    listOfNotNull(
        "HPEXPIREAT",
        key,
        instant.toEpochMilliseconds(),
        updateType,
        "FIELDS",
        field.size,
        *field,
    ),
).unwrapList()

suspend fun ReThis.hPExpireTime(
    key: String,
    vararg field: String,
): List<Long> = execute(
    listOf(
        "HPEXPIRETIME",
        key,
        "FIELDS",
        field.size,
        *field,
    ),
).unwrapList<Long>()

suspend fun ReThis.hPTTL(
    key: String,
    vararg field: String,
): List<Long> = execute(
    listOf(
        "HPTTL",
        key,
        "FIELDS",
        field.size,
        *field,
    ),
).unwrapList<Long>()

suspend fun ReThis.hRandField(
    key: String,
): String? = execute(
    listOf(
        "HRANDFIELD",
        key,
    ),
).unwrap()

suspend fun ReThis.hRandField(
    key: String,
    count: Long,
    withValues: Boolean = false,
): String? = execute(
    listOfNotNull(
        "HRANDFIELD",
        key,
        count,
        withValues.takeIf { it }?.let { "WITHVALUES" },
    ),
).unwrap()

suspend fun ReThis.hScan(
    key: String,
    cursor: Long,
    vararg option: HScanOption,
): ScanResult<Pair<String, String>> {
    val response = execute(listOf("HSCAN", key, cursor, *option))

    val arrResponse = response.safeCast<RArray>()?.value ?: exception { "Wrong response type" }
    val newCursor = arrResponse[0].unwrap<String>() ?: exception { "Missing cursor in response" }

    val keysArray = arrResponse[1].safeCast<RArray>()?.value ?: exception { "Missing keys in response" }
    val keys = keysArray.chunked(2) { it.first().unwrap<String>()!! to it.last().unwrap<String>()!! }

    return ScanResult(cursor = newCursor, keys = keys)
}

suspend fun ReThis.hSet(key: String, vararg fieldValue: Pair<String, String>): Long? = execute(
    listOf(
        "HSET",
        key,
        *fieldValue,
    ),
).unwrap()

suspend fun ReThis.hSetNx(key: String, pair: Pair<String, String>): Long? = execute(
    listOf(
        "HSETNX",
        key,
        pair,
    ),
).unwrap()

suspend fun ReThis.hStrlen(key: String, field: String): Long = execute(
    listOf(
        "HSTRLEN",
        key,
        field,
    ),
).unwrap() ?: 0

suspend fun ReThis.hTTL(
    key: String,
    vararg field: String,
): List<Long> = execute(
    listOf(
        "HTTL",
        key,
        "FIELDS",
        field.size,
        *field,
    ),
).unwrapList<Long>()

suspend fun ReThis.hVals(key: String): List<String> = execute(
    listOf(
        "HVALS",
        key,
    ),
).unwrapList<String>()
