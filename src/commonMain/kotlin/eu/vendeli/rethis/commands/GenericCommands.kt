package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.processingException
import eu.vendeli.rethis.types.common.ScanResult
import eu.vendeli.rethis.types.common.WaitAofResult
import eu.vendeli.rethis.types.core.*
import eu.vendeli.rethis.types.options.*
import eu.vendeli.rethis.utils.safeCast
import eu.vendeli.rethis.utils.writeArgument
import kotlinx.datetime.Instant
import kotlin.Long
import kotlin.time.Duration
import eu.vendeli.rethis.utils.execute

suspend fun ReThis.copy(source: String, destination: String, vararg option: CopyOption): Boolean = execute<Long>(
    mutableListOf(
        "COPY".toArgument(),
        source.toArgument(),
        destination.toArgument(),
    ).apply { option.forEach { writeArgument(it) } },
) == 1L

suspend fun ReThis.del(vararg key: String): Long = execute<Long>(
    listOf(
        "DEL".toArgument(),
        *key.toArgument(),
    ),
) ?: 0

suspend fun ReThis.dump(key: String): ByteArray? = execute(
    listOf(
        "DUMP".toArgument(),
        key.toArgument(),
    ),
    rawMarker = Unit,
).safeCast<RType.Raw>()?.value

suspend fun ReThis.exists(vararg key: String): Long = execute<Long>(
    listOf(
        "EXISTS".toArgument(),
        *key.toArgument(),
    ),
) ?: 0

suspend fun ReThis.expire(key: String, seconds: Long, option: UpdateStrategyOption? = null): Boolean = execute<Long>(
    mutableListOf(
        "EXPIRE".toArgument(),
        key.toArgument(),
        seconds.toArgument(),
    ).writeArgument(option),
) == 1L

suspend fun ReThis.expireAt(key: String, unixStamp: Instant, option: UpdateStrategyOption? = null): Boolean =
    execute<Long>(
        mutableListOf(
            "EXPIREAT".toArgument(),
            key.toArgument(),
            unixStamp.epochSeconds.toArgument(),
        ).writeArgument(option),
    ) == 1L

suspend fun ReThis.expireTime(key: String): Long? = execute<Long>(
    listOf(
        "EXPIRETIME".toArgument(),
        key.toArgument(),
    ),
)

suspend fun ReThis.keys(pattern: String): List<String> = execute<String>(
    listOf(
        "KEYS".toArgument(),
        pattern.toArgument(),
    ),
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.migrate(
    host: String,
    port: Int,
    key: String,
    destinationDb: Long,
    timeout: Duration,
    vararg option: MigrateOption,
): String? = execute<String>(
    mutableListOf(
        "MIGRATE".toArgument(),
        host.toArgument(),
        port.toArgument(),
        key.toArgument(),
        destinationDb.toArgument(),
        timeout.inWholeMilliseconds.toArgument(),
    ).apply { option.forEach { writeArgument(it) } },
)

suspend fun ReThis.move(key: String, db: Long): Boolean = execute<Long>(
    listOf(
        "MOVE".toArgument(),
        key.toArgument(),
        db.toArgument(),
    ),
) == 1L

suspend fun ReThis.objectEncoding(key: String): String? = execute<String>(
    listOf(
        "OBJECT".toArgument(),
        "ENCODING".toArgument(),
        key.toArgument(),
    ),
)

suspend fun ReThis.objectFreq(key: String): Long? = execute<Long>(
    listOf(
        "OBJECT".toArgument(),
        "FREQ".toArgument(),
        key.toArgument(),
    ),
)

suspend fun ReThis.objectIdleTime(key: String): Long? = execute<Long>(
    listOf(
        "OBJECT".toArgument(),
        "IDLETIME".toArgument(),
        key.toArgument(),
    ),
)

suspend fun ReThis.objectRefCount(key: String): Long? = execute<Long>(
    listOf(
        "OBJECT".toArgument(),
        "REFCOUNT".toArgument(),
        key.toArgument(),
    ),
)

suspend fun ReThis.persist(key: String): Boolean = execute<Long>(
    listOf(
        "PERSIST".toArgument(),
        key.toArgument(),
    ),
) == 1L

suspend fun ReThis.pExpire(
    key: String,
    milliseconds: Long,
    option: UpdateStrategyOption? = null,
): Boolean = execute<Long>(
    mutableListOf(
        "PEXPIRE".toArgument(),
        key.toArgument(),
        milliseconds.toArgument(),
    ).writeArgument(option),
) == 1L

suspend fun ReThis.pExpireAt(
    key: String,
    unixStampMillis: Long,
    option: UpdateStrategyOption? = null,
): Boolean = execute<Long>(
    mutableListOf(
        "PEXPIRE".toArgument(),
        key.toArgument(),
        unixStampMillis.toArgument(),
    ).writeArgument(option),
) == 1L

suspend fun ReThis.pExpireAt(
    key: String,
    unixStamp: Instant,
    option: UpdateStrategyOption? = null,
): Boolean = execute<Long>(
    mutableListOf(
        "PEXPIRE".toArgument(),
        key.toArgument(),
        unixStamp.toEpochMilliseconds().toArgument(),
    ).writeArgument(option),
) == 1L

suspend fun ReThis.pExpireTime(key: String): Long? = execute<Long>(
    listOf(
        "PEXPIRETIME".toArgument(),
        key.toArgument(),
    ),
)

suspend fun ReThis.pTTL(key: String): Long? = execute<Long>(
    listOf(
        "PTTL".toArgument(),
        key.toArgument(),
    ),
)

suspend fun ReThis.randomKey(): String? = execute<String>(
    listOf(
        "RANDOMKEY".toArgument(),
    ),
)

suspend fun ReThis.rename(key: String, newKey: String): Boolean = execute<String>(
    listOf(
        "RENAME".toArgument(),
        key.toArgument(),
        newKey.toArgument(),
    ),
) == "OK"

suspend fun ReThis.renameNx(key: String, newKey: String): Boolean = execute<Long>(
    listOf(
        "RENAMENX".toArgument(),
        key.toArgument(),
        newKey.toArgument(),
    ),
) == 1L

suspend fun ReThis.restore(
    key: String,
    ttl: Long,
    serializedValue: ByteArray,
    vararg options: RestoreOption,
): Boolean = execute<String>(
    mutableListOf(
        "RESTORE".toArgument(),
        key.toArgument(),
        ttl.toArgument(),
        serializedValue.toArgument(),
    ).writeArgument(options),
) == "OK"

suspend fun ReThis.scan(
    cursor: Long,
    vararg option: ScanOption,
): ScanResult<String> {
    val response =
        execute(mutableListOf("SCAN".toArgument(), cursor.toArgument()).apply { option.forEach { writeArgument(it) } })

    val arrResponse = response.safeCast<RArray>()?.value ?: processingException { "Wrong response type" }
    val newCursor = arrResponse[0].unwrap<String>() ?: processingException { "Missing cursor in response" }

    val keysArray = arrResponse[1].safeCast<RArray>()?.value ?: processingException { "Missing keys in response" }
    val keys = keysArray.map { it.unwrap<String>() ?: processingException { "Invalid key format" } }

    return ScanResult(cursor = newCursor, keys = keys)
}

suspend fun ReThis.sort(
    key: String,
    vararg option: SortOption,
): List<String> = execute<String>(
    mutableListOf("SORT".toArgument(), key.toArgument()).apply { option.forEach { writeArgument(it) } },
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.sort(
    key: String,
    store: SortOption.STORE,
    vararg option: SortOption,
): Long = execute<Long>(
    mutableListOf("SORT".toArgument(), key.toArgument()).apply {
        writeArgument(store)
        option.forEach { writeArgument(it) }
    },
) ?: 0

suspend fun ReThis.sortRo(
    key: String,
    vararg option: SortRoOption,
): List<String> = execute(
    mutableListOf("SORT_RO".toArgument(), key.toArgument()).apply { option.forEach { writeArgument(it) } },
).unwrapList<String>()

suspend fun ReThis.touch(vararg key: String): Long = execute<Long>(
    listOf(
        "TOUCH".toArgument(),
        *key.toArgument(),
    ),
) ?: 0

suspend fun ReThis.ttl(key: String): Long? = execute<Long>(
    listOf(
        "TTL".toArgument(),
        key.toArgument(),
    ),
)

suspend fun ReThis.type(key: String): String? = execute<String>(
    listOf(
        "TYPE".toArgument(),
        key.toArgument(),
    ),
)

suspend fun ReThis.unlink(vararg key: String): Long = execute<Long>(
    listOf(
        "UNLINK".toArgument(),
        *key.toArgument(),
    ),
) ?: 0

suspend fun ReThis.wait(numReplicas: Long, timeout: Long): Long = execute<Long>(
    listOf(
        "WAIT".toArgument(),
        numReplicas.toArgument(),
        timeout.toArgument(),
    ),
) ?: 0

suspend fun ReThis.waitAof(numLocal: Long, numReplicas: Long, timeout: Long): WaitAofResult = execute(
    listOf(
        "WAITAOF".toArgument(),
        numLocal.toArgument(),
        numReplicas.toArgument(),
        timeout.toArgument(),
    ),
).unwrapList<Long>().let {
    WaitAofResult(
        fsyncedRedises = it.first(),
        fsyncedReplicas = it.last(),
    )
}
