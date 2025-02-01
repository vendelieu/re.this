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
        "COPY".toArg(),
        source.toArg(),
        destination.toArg(),
    ).apply { option.forEach { writeArgument(it) } },
) == 1L

suspend fun ReThis.del(vararg key: String): Long = execute<Long>(
    listOf(
        "DEL".toArg(),
        *key.toArgument(),
    ),
) ?: 0

suspend fun ReThis.dump(key: String): ByteArray? = execute(
    listOf(
        "DUMP".toArg(),
        key.toArg(),
    ),
    rawMarker = Unit,
).safeCast<RType.Raw>()?.value

suspend fun ReThis.exists(vararg key: String): Long = execute<Long>(
    listOf(
        "EXISTS".toArg(),
        *key.toArgument(),
    ),
) ?: 0

suspend fun ReThis.expire(key: String, seconds: Long, option: UpdateStrategyOption? = null): Boolean = execute<Long>(
    mutableListOf(
        "EXPIRE".toArg(),
        key.toArg(),
        seconds.toArg(),
    ).writeArgument(option),
) == 1L

suspend fun ReThis.expireAt(key: String, unixStamp: Instant, option: UpdateStrategyOption? = null): Boolean =
    execute<Long>(
        mutableListOf(
            "EXPIREAT".toArg(),
            key.toArg(),
            unixStamp.epochSeconds.toArg(),
        ).writeArgument(option),
    ) == 1L

suspend fun ReThis.expireTime(key: String): Long? = execute<Long>(
    listOf(
        "EXPIRETIME".toArg(),
        key.toArg(),
    ),
)

suspend fun ReThis.keys(pattern: String): List<String> = execute<String>(
    listOf(
        "KEYS".toArg(),
        pattern.toArg(),
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
        "MIGRATE".toArg(),
        host.toArg(),
        port.toArg(),
        key.toArg(),
        destinationDb.toArg(),
        timeout.inWholeMilliseconds.toArg(),
    ).apply { option.forEach { writeArgument(it) } },
)

suspend fun ReThis.move(key: String, db: Long): Boolean = execute<Long>(
    listOf(
        "MOVE".toArg(),
        key.toArg(),
        db.toArg(),
    ),
) == 1L

suspend fun ReThis.objectEncoding(key: String): String? = execute<String>(
    listOf(
        "OBJECT".toArg(),
        "ENCODING".toArg(),
        key.toArg(),
    ),
)

suspend fun ReThis.objectFreq(key: String): Long? = execute<Long>(
    listOf(
        "OBJECT".toArg(),
        "FREQ".toArg(),
        key.toArg(),
    ),
)

suspend fun ReThis.objectIdleTime(key: String): Long? = execute<Long>(
    listOf(
        "OBJECT".toArg(),
        "IDLETIME".toArg(),
        key.toArg(),
    ),
)

suspend fun ReThis.objectRefCount(key: String): Long? = execute<Long>(
    listOf(
        "OBJECT".toArg(),
        "REFCOUNT".toArg(),
        key.toArg(),
    ),
)

suspend fun ReThis.persist(key: String): Boolean = execute<Long>(
    listOf(
        "PERSIST".toArg(),
        key.toArg(),
    ),
) == 1L

suspend fun ReThis.pExpire(
    key: String,
    milliseconds: Long,
    option: UpdateStrategyOption? = null,
): Boolean = execute<Long>(
    mutableListOf(
        "PEXPIRE".toArg(),
        key.toArg(),
        milliseconds.toArg(),
    ).writeArgument(option),
) == 1L

suspend fun ReThis.pExpireAt(
    key: String,
    unixStampMillis: Long,
    option: UpdateStrategyOption? = null,
): Boolean = execute<Long>(
    mutableListOf(
        "PEXPIRE".toArg(),
        key.toArg(),
        unixStampMillis.toArg(),
    ).writeArgument(option),
) == 1L

suspend fun ReThis.pExpireAt(
    key: String,
    unixStamp: Instant,
    option: UpdateStrategyOption? = null,
): Boolean = execute<Long>(
    mutableListOf(
        "PEXPIRE".toArg(),
        key.toArg(),
        unixStamp.toEpochMilliseconds().toArg(),
    ).writeArgument(option),
) == 1L

suspend fun ReThis.pExpireTime(key: String): Long? = execute<Long>(
    listOf(
        "PEXPIRETIME".toArg(),
        key.toArg(),
    ),
)

suspend fun ReThis.pTTL(key: String): Long? = execute<Long>(
    listOf(
        "PTTL".toArg(),
        key.toArg(),
    ),
)

suspend fun ReThis.randomKey(): String? = execute<String>(
    listOf(
        "RANDOMKEY".toArg(),
    ),
)

suspend fun ReThis.rename(key: String, newKey: String): Boolean = execute<String>(
    listOf(
        "RENAME".toArg(),
        key.toArg(),
        newKey.toArg(),
    ),
) == "OK"

suspend fun ReThis.renameNx(key: String, newKey: String): Boolean = execute<Long>(
    listOf(
        "RENAMENX".toArg(),
        key.toArg(),
        newKey.toArg(),
    ),
) == 1L

suspend fun ReThis.restore(
    key: String,
    ttl: Long,
    serializedValue: ByteArray,
    vararg options: RestoreOption,
): Boolean = execute<String>(
    mutableListOf(
        "RESTORE".toArg(),
        key.toArg(),
        ttl.toArg(),
        serializedValue.toArg(),
    ).writeArgument(options),
) == "OK"

suspend fun ReThis.scan(
    cursor: Long,
    vararg option: ScanOption,
): ScanResult<String> {
    val response = execute(mutableListOf("SCAN".toArg(), cursor.toArg()).apply { option.forEach { writeArgument(it) } })

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
    mutableListOf("SORT".toArg(), key.toArg()).apply { option.forEach { writeArgument(it) } },
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.sort(
    key: String,
    store: SortOption.STORE,
    vararg option: SortOption,
): Long = execute<Long>(
    mutableListOf("SORT".toArg(), key.toArg()).apply {
        writeArgument(store)
        option.forEach { writeArgument(it) }
    },
) ?: 0

suspend fun ReThis.sortRo(
    key: String,
    vararg option: SortRoOption,
): List<String> = execute(
    mutableListOf("SORT_RO".toArg(), key.toArg()).apply { option.forEach { writeArgument(it) } },
).unwrapList<String>()

suspend fun ReThis.touch(vararg key: String): Long = execute<Long>(
    listOf(
        "TOUCH".toArg(),
        *key.toArgument(),
    ),
) ?: 0

suspend fun ReThis.ttl(key: String): Long? = execute<Long>(
    listOf(
        "TTL".toArg(),
        key.toArg(),
    ),
)

suspend fun ReThis.type(key: String): String? = execute<String>(
    listOf(
        "TYPE".toArg(),
        key.toArg(),
    ),
)

suspend fun ReThis.unlink(vararg key: String): Long = execute<Long>(
    listOf(
        "UNLINK".toArg(),
        *key.toArgument(),
    ),
) ?: 0

suspend fun ReThis.wait(numReplicas: Long, timeout: Long): Long = execute<Long>(
    listOf(
        "WAIT".toArg(),
        numReplicas.toArg(),
        timeout.toArg(),
    ),
) ?: 0

suspend fun ReThis.waitAof(numLocal: Long, numReplicas: Long, timeout: Long): WaitAofResult = execute(
    listOf(
        "WAITAOF".toArg(),
        numLocal.toArg(),
        numReplicas.toArg(),
        timeout.toArg(),
    ),
).unwrapList<Long>().let {
    WaitAofResult(
        fsyncedRedises = it.first(),
        fsyncedReplicas = it.last(),
    )
}
