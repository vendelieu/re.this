package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.exception
import eu.vendeli.rethis.types.common.ScanResult
import eu.vendeli.rethis.types.common.WaitAofResult
import eu.vendeli.rethis.types.core.*
import eu.vendeli.rethis.types.core.unwrap
import eu.vendeli.rethis.types.options.*
import eu.vendeli.rethis.utils.safeCast
import eu.vendeli.rethis.utils.writeArg
import kotlinx.datetime.Instant
import kotlin.time.Duration

suspend fun ReThis.copy(source: String, destination: String, vararg option: CopyOption): Boolean = execute(
    mutableListOf(
        "COPY".toArg(),
        source.toArg(),
        destination.toArg(),
    ).apply { option.forEach { writeArg(it) } },
).unwrap<Long>() == 1L

suspend fun ReThis.del(vararg key: String): Long = execute(
    listOf(
        "DEL".toArg(),
        *key.toArg(),
    ),
).unwrap() ?: 0

suspend fun ReThis.dump(key: String): ByteArray? = execute(
    listOf(
        "DUMP".toArg(),
        key.toArg(),
    ),
    rawResponse = true,
).safeCast<RType.Raw>()?.value

suspend fun ReThis.exists(vararg key: String): Long = execute(
    listOf(
        "EXISTS".toArg(),
        *key.toArg(),
    ),
).unwrap() ?: 0

suspend fun ReThis.expire(key: String, seconds: Long, option: UpdateStrategyOption? = null): Boolean = execute(
    mutableListOf(
        "EXPIRE".toArg(),
        key.toArg(),
        seconds.toArg(),
    ).writeArg(option),
).unwrap<Long>() == 1L

suspend fun ReThis.expireAt(key: String, unixStamp: Instant, option: UpdateStrategyOption? = null): Boolean = execute(
    mutableListOf(
        "EXPIREAT".toArg(),
        key.toArg(),
        unixStamp.epochSeconds.toArg(),
    ).writeArg(option),
).unwrap<Long>() == 1L

suspend fun ReThis.expireTime(key: String): Long? = execute(
    listOf(
        "EXPIRETIME".toArg(),
        key.toArg(),
    ),
).unwrap()

suspend fun ReThis.keys(pattern: String): List<String> = execute(
    listOf(
        "KEYS".toArg(),
        pattern.toArg(),
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
    mutableListOf(
        "MIGRATE".toArg(),
        host.toArg(),
        port.toArg(),
        key.toArg(),
        destinationDb.toArg(),
        timeout.inWholeMilliseconds.toArg(),
    ).apply { option.forEach { writeArg(it) } },
).unwrap()

suspend fun ReThis.move(key: String, db: Long): Boolean = execute(
    listOf(
        "MOVE".toArg(),
        key.toArg(),
        db.toArg(),
    ),
).unwrap<Long>() == 1L

suspend fun ReThis.objectEncoding(key: String): String? = execute(
    listOf(
        "OBJECT".toArg(),
        "ENCODING".toArg(),
        key.toArg(),
    ),
).unwrap()

suspend fun ReThis.objectFreq(key: String): Long? = execute(
    listOf(
        "OBJECT".toArg(),
        "FREQ".toArg(),
        key.toArg(),
    ),
).unwrap()

suspend fun ReThis.objectIdleTime(key: String): Long? = execute(
    listOf(
        "OBJECT".toArg(),
        "IDLETIME".toArg(),
        key.toArg(),
    ),
).unwrap()

suspend fun ReThis.objectRefCount(key: String): Long? = execute(
    listOf(
        "OBJECT".toArg(),
        "REFCOUNT".toArg(),
        key.toArg(),
    ),
).unwrap()

suspend fun ReThis.persist(key: String): Boolean = execute(
    listOf(
        "PERSIST".toArg(),
        key.toArg(),
    ),
).unwrap<Long>() == 1L

suspend fun ReThis.pExpire(key: String, milliseconds: Long, option: UpdateStrategyOption? = null): Boolean = execute(
    mutableListOf(
        "PEXPIRE".toArg(),
        key.toArg(),
        milliseconds.toArg(),
    ).writeArg(option),
).unwrap<Long>() == 1L

suspend fun ReThis.pExpireAt(
    key: String,
    unixStampMillis: Long,
    option: UpdateStrategyOption? = null,
): Boolean = execute(
    mutableListOf(
        "PEXPIRE".toArg(),
        key.toArg(),
        unixStampMillis.toArg(),
    ).writeArg(option),
).unwrap<Long>() == 1L

suspend fun ReThis.pExpireAt(key: String, unixStamp: Instant, option: UpdateStrategyOption? = null): Boolean = execute(
    mutableListOf(
        "PEXPIRE".toArg(),
        key.toArg(),
        unixStamp.toEpochMilliseconds().toArg(),
    ).writeArg(option),
).unwrap<Long>() == 1L

suspend fun ReThis.pExpireTime(key: String): Long? = execute(
    listOf(
        "PEXPIRETIME".toArg(),
        key.toArg(),
    ),
).unwrap<Long>()

suspend fun ReThis.pTTL(key: String): Long? = execute(
    listOf(
        "PTTL".toArg(),
        key.toArg(),
    ),
).unwrap()

suspend fun ReThis.randomKey(): String? = execute(
    listOf(
        "RANDOMKEY".toArg(),
    ),
).unwrap()

suspend fun ReThis.rename(key: String, newKey: String): String? = execute(
    listOf(
        "RENAME".toArg(),
        key.toArg(),
        newKey.toArg(),
    ),
).unwrap()

suspend fun ReThis.renameNx(key: String, newKey: String): Boolean = execute(
    listOf(
        "RENAMENX".toArg(),
        key.toArg(),
        newKey.toArg(),
    ),
).unwrap<Long>() == 1L

suspend fun ReThis.restore(
    key: String,
    ttl: Long,
    serializedValue: ByteArray,
    vararg options: RestoreOption,
): String? = execute(
    mutableListOf(
        "RESTORE".toArg(),
        key.toArg(),
        ttl.toArg(),
        serializedValue.toArg(),
    ).writeArg(options),
).unwrap()

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
): List<String> = execute(
    mutableListOf("SORT".toArg(), key.toArg()).apply { option.forEach { writeArg(it) } },
).unwrapList<String>()

suspend fun ReThis.sort(
    key: String,
    store: SortOption.STORE,
    vararg option: SortOption,
): Long = execute(
    mutableListOf("SORT".toArg(), key.toArg()).apply {
        writeArg(store)
        option.forEach { writeArg(it) }
    },
).unwrap() ?: 0

suspend fun ReThis.sortRo(
    key: String,
    vararg option: SortRoOption,
): List<String> = execute(
    mutableListOf("SORT_RO".toArg(), key.toArg()).apply { option.forEach { writeArg(it) } },
).unwrapList<String>()

suspend fun ReThis.touch(vararg key: String): Long = execute(
    listOf(
        "TOUCH".toArg(),
        *key.toArg(),
    ),
).unwrap() ?: 0

suspend fun ReThis.ttl(key: String): Long? = execute(
    listOf(
        "TTL".toArg(),
        key.toArg(),
    ),
).unwrap<Long>()

suspend fun ReThis.type(key: String): String? = execute(
    listOf(
        "TYPE".toArg(),
        key.toArg(),
    ),
).unwrap()

suspend fun ReThis.unlink(vararg key: String): Long = execute(
    listOf(
        "UNLINK".toArg(),
        *key.toArg(),
    ),
).unwrap() ?: 0

suspend fun ReThis.wait(numReplicas: Long, timeout: Long): Long = execute(
    listOf(
        "WAIT".toArg(),
        numReplicas.toArg(),
        timeout.toArg(),
    ),
).unwrap() ?: 0

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
