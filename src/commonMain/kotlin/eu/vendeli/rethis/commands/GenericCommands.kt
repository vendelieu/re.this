package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.exception
import eu.vendeli.rethis.types.common.ScanResult
import eu.vendeli.rethis.types.common.WaitAofResult
import eu.vendeli.rethis.types.core.*
import eu.vendeli.rethis.types.options.*
import eu.vendeli.rethis.utils.safeCast
import eu.vendeli.rethis.utils.writeArg
import kotlinx.datetime.Instant
import kotlinx.io.Buffer
import kotlin.Long
import kotlin.time.Duration

suspend fun ReThis.copy(source: String, destination: String, vararg option: CopyOption): Boolean = execute<Long>(
    mutableListOf(
        "COPY".toArg(),
        source.toArg(),
        destination.toArg(),
    ).apply { option.forEach { writeArg(it) } },
) == 1L

suspend fun ReThis.del(vararg key: String): Long = execute<Long>(
    listOf(
        "DEL".toArg(),
        *key.toArg(),
    ),
) ?: 0

suspend fun ReThis.dump(key: String): Buffer? = execute(
    listOf(
        "DUMP".toArg(),
        key.toArg(),
    ),
    rawResponse = true,
).safeCast<RType.Raw>()?.value

suspend fun ReThis.exists(vararg key: String): Long = execute<Long>(
    listOf(
        "EXISTS".toArg(),
        *key.toArg(),
    ),
) ?: 0

suspend fun ReThis.expire(key: String, seconds: Long, option: UpdateStrategyOption? = null): Boolean = execute<Long>(
    mutableListOf(
        "EXPIRE".toArg(),
        key.toArg(),
        seconds.toArg(),
    ).writeArg(option),
) == 1L

suspend fun ReThis.expireAt(key: String, unixStamp: Instant, option: UpdateStrategyOption? = null): Boolean =
    execute<Long>(
        mutableListOf(
            "EXPIREAT".toArg(),
            key.toArg(),
            unixStamp.epochSeconds.toArg(),
        ).writeArg(option),
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
    ).apply { option.forEach { writeArg(it) } },
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
    ).writeArg(option),
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
    ).writeArg(option),
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
    ).writeArg(option),
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

suspend fun ReThis.rename(key: String, newKey: String): String? = execute<String>(
    listOf(
        "RENAME".toArg(),
        key.toArg(),
        newKey.toArg(),
    ),
)

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
): String? = execute<String>(
    mutableListOf(
        "RESTORE".toArg(),
        key.toArg(),
        ttl.toArg(),
        serializedValue.toArg(),
    ).writeArg(options),
)

suspend fun ReThis.scan(
    cursor: Long,
    vararg option: ScanOption,
): ScanResult<String> {
    val response = execute(mutableListOf("SCAN".toArg(), cursor.toArg()).apply { option.forEach { writeArg(it) } })

    val arrResponse = response.safeCast<RArray>()?.value ?: exception { "Wrong response type" }
    val newCursor = arrResponse[0].unwrap<String>() ?: exception { "Missing cursor in response" }

    val keysArray = arrResponse[1].safeCast<RArray>()?.value ?: exception { "Missing keys in response" }
    val keys = keysArray.map { it.unwrap<String>() ?: exception { "Invalid key format" } }

    return ScanResult(cursor = newCursor, keys = keys)
}

suspend fun ReThis.sort(
    key: String,
    vararg option: SortOption,
): List<String> = execute<String>(
    mutableListOf("SORT".toArg(), key.toArg()).apply { option.forEach { writeArg(it) } },
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.sort(
    key: String,
    store: SortOption.STORE,
    vararg option: SortOption,
): Long = execute<Long>(
    mutableListOf("SORT".toArg(), key.toArg()).apply {
        writeArg(store)
        option.forEach { writeArg(it) }
    },
) ?: 0

suspend fun ReThis.sortRo(
    key: String,
    vararg option: SortRoOption,
): List<String> = execute(
    mutableListOf("SORT_RO".toArg(), key.toArg()).apply { option.forEach { writeArg(it) } },
).unwrapList<String>()

suspend fun ReThis.touch(vararg key: String): Long = execute<Long>(
    listOf(
        "TOUCH".toArg(),
        *key.toArg(),
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
        *key.toArg(),
    ),
) ?: 0

suspend fun ReThis.wait(numReplicas: Long, timeout: Long): Long = execute<Long>(
    listOf(
        "WAIT".toArg(),
        numReplicas.toArg(),
        timeout.toArg(),
    ),
) ?: 0

suspend fun ReThis.waitAof(numLocal: Long, numReplicas: Long, timeout: Long): WaitAofResult? = execute<Long>(
    listOf(
        "WAITAOF".toArg(),
        numLocal.toArg(),
        numReplicas.toArg(),
        timeout.toArg(),
    ),
    isCollectionResponse = true,
)?.let {
    WaitAofResult(
        fsyncedRedises = it.first(),
        fsyncedReplicas = it.last(),
    )
}
