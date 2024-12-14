package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.exception
import eu.vendeli.rethis.types.common.ScanResult
import eu.vendeli.rethis.types.core.RArray
import eu.vendeli.rethis.types.core.toArg
import eu.vendeli.rethis.types.core.unwrap
import eu.vendeli.rethis.types.core.unwrapList
import eu.vendeli.rethis.types.options.HScanOption
import eu.vendeli.rethis.types.options.UpdateStrategyOption
import eu.vendeli.rethis.utils.safeCast
import eu.vendeli.rethis.utils.unwrapRespIndMap
import eu.vendeli.rethis.utils.writeArg
import kotlinx.datetime.Instant
import kotlin.Long
import kotlin.time.Duration

suspend fun ReThis.hDel(key: String, vararg field: String): Long = execute<Long>(
    listOf(
        "HDEL".toArg(),
        key.toArg(),
        *field.toArg(),
    ),
) ?: 0

suspend fun ReThis.hExists(key: String, field: String): Boolean = execute<Long>(
    listOf(
        "HEXISTS".toArg(),
        key.toArg(),
        field.toArg(),
    ),
) == 1L

suspend fun ReThis.hExpire(
    key: String,
    seconds: Duration,
    vararg field: String,
    updateType: UpdateStrategyOption? = null,
): List<Long> = execute(
    mutableListOf(
        "HEXPIRE".toArg(),
        key.toArg(),
        seconds.inWholeSeconds.toArg(),
    ).apply {
        writeArg(updateType)
        writeArg("FIELDS")
        writeArg(field.size)
        writeArg(field)
    },
).unwrapList()

suspend fun ReThis.hExpireAt(
    key: String,
    instant: Instant,
    vararg field: String,
    updateType: UpdateStrategyOption? = null,
): List<Long> = execute(
    mutableListOf(
        "HEXPIREAT".toArg(),
        key.toArg(),
        instant.epochSeconds.toArg(),
    ).apply {
        writeArg(updateType)
        writeArg("FIELDS")
        writeArg(field.size)
        writeArg(field)
    },
).unwrapList()

suspend fun ReThis.hExpireTime(
    key: String,
    vararg field: String,
): List<Long> = execute(
    mutableListOf(
        "HEXPIRETIME".toArg(),
        key.toArg(),
    ).apply {
        writeArg("FIELDS")
        writeArg(field.size)
        writeArg(field)
    },
).unwrapList()

suspend fun ReThis.hGet(key: String, field: String): String? = execute<String>(
    listOf(
        "HGET".toArg(),
        key.toArg(),
        field.toArg(),
    ),
)

suspend fun ReThis.hGetAll(key: String): Map<String, String?>? = execute(
    listOf(
        "HGETALL".toArg(),
        key.toArg(),
    ),
).unwrapRespIndMap()

suspend fun ReThis.hIncrBy(key: String, field: String, increment: Long): Long = execute<Long>(
    listOf(
        "HINCRBY".toArg(),
        key.toArg(),
        field.toArg(),
        increment.toArg(),
    ),
) ?: 0

suspend fun ReThis.hIncrByFloat(key: String, field: String, increment: Double): Double? = execute<Double>(
    listOf(
        "HINCRBYFLOAT".toArg(),
        key.toArg(),
        field.toArg(),
        increment.toArg(),
    ),
)

suspend fun ReThis.hKeys(key: String): List<String> = execute(
    listOf(
        "HKEYS".toArg(),
        key.toArg(),
    ),
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.hLen(key: String): Long = execute<Long>(
    listOf(
        "HLEN".toArg(),
        key.toArg(),
    ),
) ?: 0

suspend fun ReThis.hMGet(key: String, vararg field: String): List<String> = execute(
    listOf(
        "HMGET".toArg(),
        key.toArg(),
        *field.toArg(),
    ),
).unwrapList()

suspend fun ReThis.hMSet(key: String, vararg fieldValue: Pair<String, String>): String? = execute<String>(
    mutableListOf(
        "HMSET".toArg(),
        key.toArg(),
    ).writeArg(fieldValue),
)

suspend fun ReThis.hPersist(
    key: String,
    vararg field: String,
): List<Long> = execute(
    mutableListOf(
        "HPERSIST".toArg(),
        key.toArg(),
    ).apply {
        writeArg("FIELDS")
        writeArg(field.size)
        writeArg(field)
    },
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.hPExpire(
    key: String,
    milliseconds: Duration,
    vararg field: String,
    updateType: UpdateStrategyOption? = null,
): List<Long> = execute(
    mutableListOf(
        "HPEXPIRE".toArg(),
        key.toArg(),
        milliseconds.inWholeMilliseconds.toArg(),
    ).apply {
        writeArg(updateType)
        writeArg("FIELDS")
        writeArg(field.size)
        writeArg(field)
    },
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.hPExpireAt(
    key: String,
    instant: Instant,
    vararg field: String,
    updateType: UpdateStrategyOption? = null,
): List<Long> = execute(
    mutableListOf(
        "HPEXPIREAT".toArg(),
        key.toArg(),
        instant.toEpochMilliseconds().toArg(),
    ).apply {
        writeArg(updateType)
        writeArg("FIELDS")
        writeArg(field.size)
        writeArg(field)
    },
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.hPExpireTime(
    key: String,
    vararg field: String,
): List<Long> = execute(
    mutableListOf(
        "HPEXPIRETIME".toArg(),
        key.toArg(),
    ).apply {
        writeArg("FIELDS")
        writeArg(field.size)
        writeArg(field)
    },
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.hPTTL(
    key: String,
    vararg field: String,
): List<Long> = execute(
    mutableListOf(
        "HPTTL".toArg(),
        key.toArg(),
    ).apply {
        writeArg("FIELDS")
        writeArg(field.size)
        writeArg(field)
    },
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.hRandField(
    key: String,
): String? = execute<String>(
    listOf(
        "HRANDFIELD".toArg(),
        key.toArg(),
    ),
)

suspend fun ReThis.hRandField(
    key: String,
    count: Long,
    withValues: Boolean = false,
): String? = execute<String>(
    mutableListOf(
        "HRANDFIELD".toArg(),
        key.toArg(),
        count.toArg(),
    ).apply {
        if (withValues) writeArg("WITHVALUES")
    },
)

suspend fun ReThis.hScan(
    key: String,
    cursor: Long,
    vararg option: HScanOption,
): ScanResult<Pair<String, String>> {
    val response = execute(mutableListOf("HSCAN".toArg(), key.toArg(), cursor.toArg()).writeArg(option))

    val arrResponse = response.safeCast<RArray>()?.value ?: exception { "Wrong response type" }
    val newCursor = arrResponse[0].unwrap<String>() ?: exception { "Missing cursor in response" }

    val keysArray = arrResponse[1].safeCast<RArray>()?.value ?: exception { "Missing keys in response" }
    val keys = keysArray.chunked(2) { it.first().unwrap<String>()!! to it.last().unwrap<String>()!! }

    return ScanResult(cursor = newCursor, keys = keys)
}

suspend fun ReThis.hSet(key: String, vararg fieldValue: Pair<String, String>): Long? = execute<Long>(
    mutableListOf(
        "HSET".toArg(),
        key.toArg(),
    ).writeArg(fieldValue),
)

suspend fun ReThis.hSetNx(key: String, pair: Pair<String, String>): Long? = execute<Long>(
    mutableListOf(
        "HSETNX".toArg(),
        key.toArg(),
    ).writeArg(pair),
)

suspend fun ReThis.hStrlen(key: String, field: String): Long = execute<Long>(
    listOf(
        "HSTRLEN".toArg(),
        key.toArg(),
        field.toArg(),
    ),
) ?: 0

suspend fun ReThis.hTTL(
    key: String,
    vararg field: String,
): List<Long> = execute(
    mutableListOf(
        "HTTL".toArg(),
        key.toArg(),
    ).apply {
        writeArg("FIELDS")
        writeArg(field.size)
        writeArg(field)
    },
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.hVals(key: String): List<String> = execute(
    listOf(
        "HVALS".toArg(),
        key.toArg(),
    ),
    isCollectionResponse = true,
) ?: emptyList()
