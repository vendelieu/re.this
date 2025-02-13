package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.processingException
import eu.vendeli.rethis.types.common.RArray
import eu.vendeli.rethis.types.common.RType
import eu.vendeli.rethis.types.common.toArgument
import eu.vendeli.rethis.types.options.HScanOption
import eu.vendeli.rethis.types.options.UpdateStrategyOption
import eu.vendeli.rethis.types.response.ScanResult
import eu.vendeli.rethis.utils.*
import kotlinx.datetime.Instant
import kotlin.time.Duration

suspend fun ReThis.hDel(key: String, vararg field: String): Long = execute<Long>(
    listOf(
        "HDEL".toArgument(),
        key.toArgument(),
        *field.toArgument(),
    ),
) ?: 0

suspend fun ReThis.hExists(key: String, field: String): Boolean = execute<Long>(
    listOf(
        "HEXISTS".toArgument(),
        key.toArgument(),
        field.toArgument(),
    ),
) == 1L

suspend fun ReThis.hExpire(
    key: String,
    seconds: Duration,
    vararg field: String,
    updateType: UpdateStrategyOption? = null,
): List<Long> = execute(
    mutableListOf(
        "HEXPIRE".toArgument(),
        key.toArgument(),
        seconds.inWholeSeconds.toArgument(),
    ).apply {
        writeArgument(updateType)
        writeArgument("FIELDS")
        writeArgument(field.size)
        writeArgument(field)
    },
).unwrapList()

suspend fun ReThis.hExpireAt(
    key: String,
    instant: Instant,
    vararg field: String,
    updateType: UpdateStrategyOption? = null,
): List<Long> = execute(
    mutableListOf(
        "HEXPIREAT".toArgument(),
        key.toArgument(),
        instant.epochSeconds.toArgument(),
    ).apply {
        writeArgument(updateType)
        writeArgument("FIELDS")
        writeArgument(field.size)
        writeArgument(field)
    },
).unwrapList()

suspend fun ReThis.hExpireTime(
    key: String,
    vararg field: String,
): List<Long> = execute(
    mutableListOf(
        "HEXPIRETIME".toArgument(),
        key.toArgument(),
    ).apply {
        writeArgument("FIELDS")
        writeArgument(field.size)
        writeArgument(field)
    },
).unwrapList()

suspend fun ReThis.hGet(key: String, field: String): String? = execute<String>(
    listOf(
        "HGET".toArgument(),
        key.toArgument(),
        field.toArgument(),
    ),
)

suspend fun ReThis.hGetAll(key: String): Map<String, String?>? = execute(
    listOf(
        "HGETALL".toArgument(),
        key.toArgument(),
    ),
).unwrapRESPAgnosticMap()

suspend fun ReThis.hIncrBy(key: String, field: String, increment: Long): Long = execute<Long>(
    listOf(
        "HINCRBY".toArgument(),
        key.toArgument(),
        field.toArgument(),
        increment.toArgument(),
    ),
) ?: 0

suspend fun ReThis.hIncrByFloat(key: String, field: String, increment: Double): Double? = execute<String>(
    listOf(
        "HINCRBYFLOAT".toArgument(),
        key.toArgument(),
        field.toArgument(),
        increment.toArgument(),
    ),
)?.toDouble()

suspend fun ReThis.hKeys(key: String): List<String> = execute(
    listOf(
        "HKEYS".toArgument(),
        key.toArgument(),
    ),
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.hLen(key: String): Long = execute<Long>(
    listOf(
        "HLEN".toArgument(),
        key.toArgument(),
    ),
) ?: 0

suspend fun ReThis.hMGet(key: String, vararg field: String): List<String?> = execute<String>(
    listOf(
        "HMGET".toArgument(),
        key.toArgument(),
        *field.toArgument(),
    ),
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.hMSet(key: String, vararg fieldValue: Pair<String, String>): Boolean = execute<String>(
    mutableListOf(
        "HMSET".toArgument(),
        key.toArgument(),
    ).writeArgument(fieldValue),
) == "OK"

suspend fun ReThis.hPersist(
    key: String,
    vararg field: String,
): List<Long> = execute(
    mutableListOf(
        "HPERSIST".toArgument(),
        key.toArgument(),
    ).apply {
        writeArgument("FIELDS")
        writeArgument(field.size)
        writeArgument(field)
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
        "HPEXPIRE".toArgument(),
        key.toArgument(),
        milliseconds.inWholeMilliseconds.toArgument(),
    ).apply {
        writeArgument(updateType)
        writeArgument("FIELDS")
        writeArgument(field.size)
        writeArgument(field)
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
        "HPEXPIREAT".toArgument(),
        key.toArgument(),
        instant.toEpochMilliseconds().toArgument(),
    ).apply {
        writeArgument(updateType)
        writeArgument("FIELDS")
        writeArgument(field.size)
        writeArgument(field)
    },
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.hPExpireTime(
    key: String,
    vararg field: String,
): List<Long> = execute(
    mutableListOf(
        "HPEXPIRETIME".toArgument(),
        key.toArgument(),
    ).apply {
        writeArgument("FIELDS")
        writeArgument(field.size)
        writeArgument(field)
    },
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.hPTTL(
    key: String,
    vararg field: String,
): List<Long> = execute(
    mutableListOf(
        "HPTTL".toArgument(),
        key.toArgument(),
    ).apply {
        writeArgument("FIELDS")
        writeArgument(field.size)
        writeArgument(field)
    },
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.hRandField(
    key: String,
): String? = execute<String>(
    listOf(
        "HRANDFIELD".toArgument(),
        key.toArgument(),
    ),
)

suspend fun ReThis.hRandField(
    key: String,
    count: Long,
    withValues: Boolean = false,
): List<RType> = execute(
    mutableListOf(
        "HRANDFIELD".toArgument(),
        key.toArgument(),
        count.toArgument(),
    ).apply {
        if (withValues) writeArgument("WITHVALUES")
    },
).unwrapList()

suspend fun ReThis.hScan(
    key: String,
    cursor: Long,
    vararg option: HScanOption,
): ScanResult<Pair<String, String>> {
    val response =
        execute(mutableListOf("HSCAN".toArgument(), key.toArgument(), cursor.toArgument()).writeArgument(option))

    val arrResponse = response.safeCast<RArray>()?.value ?: processingException { "Wrong response type" }
    val newCursor = arrResponse[0].unwrap<String>() ?: processingException { "Missing cursor in response" }

    val keysArray = arrResponse[1].safeCast<RArray>()?.value ?: processingException { "Missing keys in response" }
    val keys = keysArray.chunked(2) { it.first().unwrap<String>()!! to it.last().unwrap<String>()!! }

    return ScanResult(cursor = newCursor, keys = keys)
}

suspend fun ReThis.hSet(key: String, vararg fieldValue: Pair<String, String>): Long? = execute<Long>(
    mutableListOf(
        "HSET".toArgument(),
        key.toArgument(),
    ).writeArgument(fieldValue),
)

suspend fun ReThis.hSetNx(key: String, pair: Pair<String, String>): Long? = execute<Long>(
    mutableListOf(
        "HSETNX".toArgument(),
        key.toArgument(),
    ).writeArgument(pair),
)

suspend fun ReThis.hStrlen(key: String, field: String): Long = execute<Long>(
    listOf(
        "HSTRLEN".toArgument(),
        key.toArgument(),
        field.toArgument(),
    ),
) ?: 0

suspend fun ReThis.hTTL(
    key: String,
    vararg field: String,
): List<Long> = execute(
    mutableListOf(
        "HTTL".toArgument(),
        key.toArgument(),
    ).apply {
        writeArgument("FIELDS")
        writeArgument(field.size)
        writeArgument(field)
    },
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.hVals(key: String): List<String> = execute(
    listOf(
        "HVALS".toArgument(),
        key.toArgument(),
    ),
    isCollectionResponse = true,
) ?: emptyList()
