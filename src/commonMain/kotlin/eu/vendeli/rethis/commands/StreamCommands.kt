package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.types.core.Argument
import eu.vendeli.rethis.types.core.RType
import eu.vendeli.rethis.types.core.toArgument
import eu.vendeli.rethis.types.core.unwrapList
import eu.vendeli.rethis.types.options.*
import eu.vendeli.rethis.utils.response.unwrapRespIndMap
import eu.vendeli.rethis.utils.writeArgument
import eu.vendeli.rethis.utils.execute

suspend fun ReThis.xAck(
    key: String,
    group: String,
    vararg id: String,
) = execute<Long>(
    listOf("XACK".toArgument(), key.toArgument(), group.toArgument(), *id.toArgument()),
)

suspend fun ReThis.xAdd(
    key: String,
    nomkstream: XAddOption.NOMKSTREAM? = null,
    trim: XAddOption.Trim? = null,
    id: XAddOption.Identifier,
    vararg entry: Pair<String, String>,
) = execute<String>(
    mutableListOf(
        "XADD".toArgument(),
        key.toArgument(),
    ).writeArgument(
        nomkstream,
        trim,
        id,
        *entry,
    ),
)

suspend fun ReThis.xAutoClaim(
    key: String,
    group: String,
    consumer: String,
    minIdleTime: Long,
    start: String,
    count: Long? = null,
    justID: Boolean = false,
) = execute(
    mutableListOf(
        "XAUTOCLAIM".toArgument(),
        key.toArgument(),
        group.toArgument(),
        consumer.toArgument(),
        minIdleTime.toArgument(),
        start.toArgument(),
    ).writeArgument(
        count?.let { "COUNT" to it },
        justID.takeIf { it }?.let { "JUSTID" },
    ),
).unwrapList<RType>()

suspend fun ReThis.xClaim(
    key: String,
    group: String,
    consumer: String,
    minIdleTime: Long,
    vararg id: String,
    idle: XClaimOption.Idle? = null,
    time: XClaimOption.Time? = null,
    retryCount: XClaimOption.RetryCount? = null,
    force: Boolean = false,
    justID: Boolean = false,
    lastId: XClaimOption.LastId? = null,
) = execute(
    mutableListOf(
        "XCLAIM".toArgument(),
        key.toArgument(),
        group.toArgument(),
        consumer.toArgument(),
        minIdleTime.toArgument(),
        *id.toArgument(),
    ).writeArgument(
        idle,
        time,
        retryCount,
        force.takeIf { it }?.let { "FORCE" },
        justID.takeIf { it }?.let { "JUSTID" },
        lastId,
    ),
).unwrapList<RType>()

suspend fun ReThis.xDel(key: String, vararg id: String) = execute<Long>(
    listOf("XDEL".toArgument(), key.toArgument(), *id.toArgument()),
)

suspend fun ReThis.xGroupCreate(
    key: String,
    group: String,
    id: XId,
    mkstream: Boolean = false,
    entriesRead: Long? = null,
): Boolean = execute<String>(
    mutableListOf(
        "XGROUP".toArgument(),
        "CREATE".toArgument(),
        key.toArgument(),
        group.toArgument(),
    ).writeArgument(
        id,
        mkstream.takeIf { it }?.let { "MKSTREAM" },
        entriesRead?.let { "ENTRIESREAD" to it },
    ),
) == "OK"

suspend fun ReThis.xGroupCreateConsumer(key: String, group: String, consumer: String): Long? = execute<Long>(
    listOf(
        "XGROUP".toArgument(),
        "CREATECONSUMER".toArgument(),
        key.toArgument(),
        group.toArgument(),
        consumer.toArgument(),
    ),
)

suspend fun ReThis.xGroupDelConsumer(key: String, group: String, consumer: String): Long? = execute<Long>(
    listOf(
        "XGROUP".toArgument(),
        "DELCONSUMER".toArgument(),
        key.toArgument(),
        group.toArgument(),
        consumer.toArgument(),
    ),
)

suspend fun ReThis.xGroupDestroy(key: String, group: String): Long? = execute<Long>(
    listOf("XGROUP".toArgument(), "DESTROY".toArgument(), key.toArgument(), group.toArgument()),
)

suspend fun ReThis.xGroupSetId(
    key: String,
    group: String,
    id: XId,
    entriesRead: Long? = null,
): Boolean = execute<String>(
    mutableListOf(
        "XGROUP".toArgument(),
        "SETID".toArgument(),
        key.toArgument(),
        group.toArgument(),
    ).writeArgument(
        id,
        "ENTRIESREAD" to entriesRead,
    ),
) == "OK"

suspend fun ReThis.xInfoConsumers(key: String, group: String): List<RType> = execute(
    listOf("XINFO".toArgument(), "CONSUMERS".toArgument(), key.toArgument(), group.toArgument()),
).unwrapList<RType>()

suspend fun ReThis.xInfoGroups(key: String): List<RType> = execute(
    listOf("XINFO".toArgument(), "GROUPS".toArgument(), key.toArgument()),
).unwrapList<RType>()

suspend fun ReThis.xInfoStream(
    key: String,
    full: Boolean = false,
    limit: XOption.Limit? = null,
): Map<String, RType?> = execute(
    mutableListOf(
        "XINFO".toArgument(),
        "STREAM".toArgument(),
        key.toArgument(),
    ).writeArgument(
        full.takeIf { it }?.let { "FULL" },
        limit,
    ),
).unwrapRespIndMap<String, RType>() ?: emptyMap()

suspend fun ReThis.xLen(key: String): Long? = execute<Long>(listOf("XLEN".toArgument(), key.toArgument()))

suspend fun ReThis.xPending(key: String, group: String, option: XPendingOption? = null): List<RType> = execute(
    mutableListOf("XPENDING".toArgument(), key.toArgument(), group.toArgument()).writeArgument(option),
).unwrapList<RType>()

suspend fun ReThis.xRange(key: String, start: String, end: String, limit: XOption.Limit? = null): List<RType> = execute(
    mutableListOf("XRANGE".toArgument(), key.toArgument(), start.toArgument(), end.toArgument()).writeArgument(limit),
).unwrapList<RType>()

suspend fun ReThis.xRead(
    keys: List<String>,
    ids: List<String>,
    count: XOption.Limit? = null,
    blockMillis: Long? = null,
): Map<String, RType?>? = execute(
    mutableListOf<Argument>(
        "XREAD".toArgument(),
    ).writeArgument(
        count,
        blockMillis?.let { "BLOCK" to it },
        "STREAMS",
        *keys.toTypedArray(),
        *ids.toTypedArray(),
    ),
).unwrapRespIndMap<String, RType>()

suspend fun ReThis.xReadGroup(
    group: String,
    consumer: String,
    count: XOption.Limit? = null,
    blockMillis: Long? = null,
    noack: Boolean = false,
    keys: List<String>,
    ids: List<String>,
): Map<String, RType?>? = execute(
    mutableListOf<Argument>(
        "XREADGROUP".toArgument(),
        "GROUP".toArgument(),
        group.toArgument(),
        consumer.toArgument(),
    ).writeArgument(
        count,
        blockMillis?.let { "BLOCK" to it },
        noack.takeIf { it }?.let { "NOACK" },
        "STREAMS",
        *keys.toTypedArray(),
        *ids.toTypedArray(),
    ),
).unwrapRespIndMap<String, RType>()

suspend fun ReThis.xRevRange(
    key: String,
    end: String,
    start: String,
    limit: XOption.Limit? = null,
): List<RType> = execute(
    mutableListOf(
        "XREVRANGE".toArgument(),
        key.toArgument(),
        end.toArgument(),
        start.toArgument(),
    ).writeArgument(limit),
).unwrapList<RType>()

suspend fun ReThis.xSetId(
    key: String,
    lastId: String,
    entriesAdded: Long? = null,
    maxDeletedId: String? = null,
): Boolean = execute<String>(
    mutableListOf(
        "XSETID".toArgument(),
        key.toArgument(),
        lastId.toArgument(),
    ).writeArgument(
        entriesAdded?.let { "ENTRIESADDED" to it },
        maxDeletedId?.let { "MAXDELETEDID" to it },
    ),
) == "OK"

suspend fun ReThis.xTrim(
    key: String,
    threshold: Long,
    trimmingStrategy: TrimmingStrategy,
    exactement: Exactement? = null,
    trim: XOption.Limit? = null,
) = execute<Long>(
    mutableListOf(
        "XTRIM".toArgument(),
        key.toArgument(),
    ).writeArgument(
        trimmingStrategy.toArgument(),
        exactement,
        threshold,
        trim,
    ),
)
