package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.types.common.ScanResult
import eu.vendeli.rethis.types.common.WaitAofResult
import eu.vendeli.rethis.exception
import eu.vendeli.rethis.types.options.*
import eu.vendeli.rethis.types.core.RArray
import eu.vendeli.rethis.types.core.RType
import eu.vendeli.rethis.types.options.UpdateStrategyOption
import eu.vendeli.rethis.types.core.unwrap
import eu.vendeli.rethis.types.core.unwrapList
import eu.vendeli.rethis.utils.safeCast
import kotlinx.datetime.Instant
import kotlin.time.Duration

suspend fun ReThis.copy(source: String, destination: String, vararg option: CopyOption): Boolean = execute(
    listOf(
        "COPY",
        source,
        destination,
        *option,
    ),
).unwrap<Long>() == 1L

suspend fun ReThis.del(vararg key: String): Long = execute(
    listOf(
        "DEL",
        *key,
    ),
).unwrap() ?: 0

suspend fun ReThis.dump(key: String): ByteArray? = execute(
    listOf(
        "DUMP",
        key,
    ),
    rawResponse = true,
).safeCast<RType.Raw>()?.value

suspend fun ReThis.exists(vararg key: String): Long = execute(
    listOf(
        "EXISTS",
        *key,
    ),
).unwrap() ?: 0

suspend fun ReThis.expire(key: String, seconds: Long, option: UpdateStrategyOption? = null): Boolean = execute(
    listOfNotNull(
        "EXPIRE",
        key,
        seconds,
        option,
    ),
).unwrap<Long>() == 1L

suspend fun ReThis.expireAt(key: String, unixStamp: Instant, option: UpdateStrategyOption? = null): Boolean = execute(
    listOfNotNull(
        "EXPIREAT",
        key,
        unixStamp.epochSeconds,
        option,
    ),
).unwrap<Long>() == 1L

suspend fun ReThis.expireTime(key: String): Long? = execute(
    listOf(
        "EXPIRETIME",
        key,
    ),
).unwrap()

suspend fun ReThis.keys(pattern: String): List<String> = execute(
    listOf(
        "KEYS",
        pattern,
    ),
).unwrapList()

suspend fun ReThis.migrate(
    host: String,
    port: Int,
    key: String,
    destinationDb: Long,
    timeout: Duration,
    vararg option: MigrateOption,
): String? = execute(
    listOf(
        "MIGRATE",
        host,
        port,
        key,
        destinationDb,
        timeout.inWholeMilliseconds,
        *option,
    ),
).unwrap()

suspend fun ReThis.move(key: String, db: Long): Boolean = execute(
    listOf(
        "MOVE",
        key,
        db,
    ),
).unwrap<Long>() == 1L

suspend fun ReThis.objectEncoding(key: String): String? = execute(
    listOf(
        "OBJECT",
        "ENCODING",
        key,
    ),
).unwrap()

suspend fun ReThis.objectFreq(key: String): Long? = execute(
    listOf(
        "OBJECT",
        "FREQ",
        key,
    ),
).unwrap()

suspend fun ReThis.objectIdleTime(key: String): Long? = execute(
    listOf(
        "OBJECT",
        "IDLETIME",
        key,
    ),
).unwrap()

suspend fun ReThis.objectRefCount(key: String): Long? = execute(
    listOf(
        "OBJECT",
        "REFCOUNT",
        key,
    ),
).unwrap()

suspend fun ReThis.persist(key: String): Boolean = execute(
    listOf(
        "PERSIST",
        key,
    ),
).unwrap<Long>() == 1L

suspend fun ReThis.pExpire(key: String, milliseconds: Long, option: UpdateStrategyOption? = null): Boolean = execute(
    listOfNotNull(
        "PEXPIRE",
        key,
        milliseconds,
        option,
    ),
).unwrap<Long>() == 1L

suspend fun ReThis.pExpireAt(key: String, unixStampMillis: Long, option: UpdateStrategyOption? = null): Boolean =
    execute(
        listOfNotNull(
            "PEXPIRE",
            key,
            unixStampMillis,
            option,
        ),
    ).unwrap<Long>() == 1L

suspend fun ReThis.pExpireAt(key: String, unixStamp: Instant, option: UpdateStrategyOption? = null): Boolean = execute(
    listOfNotNull(
        "PEXPIRE",
        key,
        unixStamp.toEpochMilliseconds(),
        option,
    ),
).unwrap<Long>() == 1L

suspend fun ReThis.pExpireTime(key: String): Long? = execute(
    listOf(
        "PEXPIRETIME",
        key,
    ),
).unwrap<Long>()

suspend fun ReThis.pTTL(key: String): Long? = execute(
    listOf(
        "PTTL",
        key,
    ),
).unwrap()

suspend fun ReThis.randomKey(): String? = execute(
    listOf(
        "RANDOMKEY",
    ),
).unwrap()

suspend fun ReThis.rename(key: String, newKey: String): String? = execute(
    listOf(
        "RENAME",
        key,
        newKey,
    ),
).unwrap()

suspend fun ReThis.renameNx(key: String, newKey: String): Boolean = execute(
    listOf(
        "RENAMENX",
        key,
        newKey,
    ),
).unwrap<Long>() == 1L

suspend fun ReThis.restore(
    key: String,
    ttl: Long,
    serializedValue: ByteArray,
    vararg options: RestoreOption,
): String? = execute(
    listOf(
        "RESTORE",
        key,
        ttl,
        serializedValue,
        *options,
    ),
).unwrap()

suspend fun ReThis.scan(
    cursor: Long,
    vararg option: ScanOption,
): ScanResult<String> {
    val response = execute(listOf("SCAN", cursor, *option))

    val arrResponse = response.safeCast<RArray>()?.value ?: exception { "Wrong response type" }
    val newCursor = arrResponse[0].unwrap<String>() ?: exception { "Missing cursor in response" }

    val keysArray = arrResponse[1].safeCast<RArray>()?.value ?: exception { "Missing keys in response" }
    val keys = keysArray.map { it.unwrap<String>() ?: exception { "Invalid key format" } }

    return ScanResult(cursor = newCursor, keys = keys)
}

suspend fun ReThis.sort(
    key: String,
    vararg option: SortOption,
): List<String> = execute(listOf("SORT", key, *option)).unwrapList<String>()

suspend fun ReThis.sort(
    key: String,
    store: SortOption.STORE,
    vararg option: SortOption,
): Long = execute(listOf("SORT", key, store, *option)).unwrap() ?: 0

suspend fun ReThis.sortRo(
    key: String,
    vararg option: SortRoOption,
): List<String> = execute(listOf("SORT_RO", key, *option)).unwrapList<String>()

suspend fun ReThis.touch(vararg key: String): Long = execute(
    listOf(
        "TOUCH",
        *key,
    ),
).unwrap() ?: 0

suspend fun ReThis.ttl(key: String): Long? = execute(
    listOf(
        "TTL",
        key,
    ),
).unwrap<Long>()

suspend fun ReThis.type(key: String): String? = execute(
    listOf(
        "TYPE",
        key,
    ),
).unwrap()

suspend fun ReThis.unlink(vararg key: String): Long = execute(
    listOf(
        "UNLINK",
        *key,
    ),
).unwrap() ?: 0

suspend fun ReThis.wait(numReplicas: Long, timeout: Long): Long = execute(
    listOf(
        "WAIT",
        numReplicas,
        timeout,
    ),
).unwrap() ?: 0

suspend fun ReThis.waitAof(numLocal: Long, numReplicas: Long, timeout: Long): WaitAofResult = execute(
    listOf(
        "WAITAOF",
        numLocal,
        numReplicas,
        timeout,
    ),
).unwrapList<Long>().let {
    WaitAofResult(
        fsyncedRedises = it.first(),
        fsyncedReplicas = it.last(),
    )
}
