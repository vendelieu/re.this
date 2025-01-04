package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.types.core.Argument
import eu.vendeli.rethis.types.core.RType
import eu.vendeli.rethis.types.core.toArg
import eu.vendeli.rethis.types.core.unwrapList
import eu.vendeli.rethis.types.options.*
import eu.vendeli.rethis.utils.unwrapRespIndMap
import eu.vendeli.rethis.utils.writeArg

suspend fun ReThis.xAck(
    key: String,
    group: String,
    vararg id: String,
) = execute<Long>(
    listOf("XACK".toArg(), key.toArg(), group.toArg(), *id.toArg()),
)

suspend fun ReThis.xAdd(
    key: String,
    nomkstream: XAddOption.NOMKSTREAM? = null,
    trim: XAddOption.Trim? = null,
    id: XAddOption.Identifier,
    vararg entry: Pair<String, String>,
) = execute<String>(
    mutableListOf(
        "XADD".toArg(),
        key.toArg(),
    ).writeArg(
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
        "XAUTOCLAIM".toArg(),
        key.toArg(),
        group.toArg(),
        consumer.toArg(),
        minIdleTime.toArg(),
        start.toArg(),
    ).writeArg(
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
        "XCLAIM".toArg(),
        key.toArg(),
        group.toArg(),
        consumer.toArg(),
        minIdleTime.toArg(),
        *id.toArg(),
    ).writeArg(
        idle,
        time,
        retryCount,
        force.takeIf { it }?.let { "FORCE" },
        justID.takeIf { it }?.let { "JUSTID" },
        lastId,
    ),
).unwrapList<RType>()

suspend fun ReThis.xDel(key: String, vararg id: String) = execute<Long>(
    listOf("XDEL".toArg(), key.toArg(), *id.toArg()),
)

suspend fun ReThis.xGroupCreate(
    key: String,
    group: String,
    id: XId,
    mkstream: Boolean = false,
    entriesRead: Long? = null,
): Boolean = execute<String>(
    mutableListOf(
        "XGROUP".toArg(),
        "CREATE".toArg(),
        key.toArg(),
        group.toArg(),
    ).writeArg(
        id,
        mkstream.takeIf { it }?.let { "MKSTREAM" },
        entriesRead?.let { "ENTRIESREAD" to it },
    ),
) == "OK"

suspend fun ReThis.xGroupCreateConsumer(key: String, group: String, consumer: String): Long? = execute<Long>(
    listOf("XGROUP".toArg(), "CREATECONSUMER".toArg(), key.toArg(), group.toArg(), consumer.toArg()),
)

suspend fun ReThis.xGroupDelConsumer(key: String, group: String, consumer: String): Long? = execute<Long>(
    listOf("XGROUP".toArg(), "DELCONSUMER".toArg(), key.toArg(), group.toArg(), consumer.toArg()),
)

suspend fun ReThis.xGroupDestroy(key: String, group: String): Long? = execute<Long>(
    listOf("XGROUP".toArg(), "DESTROY".toArg(), key.toArg(), group.toArg()),
)

suspend fun ReThis.xGroupSetId(
    key: String,
    group: String,
    id: XId,
    entriesRead: Long? = null,
): Boolean = execute<String>(
    mutableListOf(
        "XGROUP".toArg(),
        "SETID".toArg(),
        key.toArg(),
        group.toArg(),
    ).writeArg(
        id,
        "ENTRIESREAD" to entriesRead,
    ),
) == "OK"

suspend fun ReThis.xInfoConsumers(key: String, group: String): List<RType> = execute(
    listOf("XINFO".toArg(), "CONSUMERS".toArg(), key.toArg(), group.toArg()),
).unwrapList<RType>()

suspend fun ReThis.xInfoGroups(key: String): List<RType> = execute(
    listOf("XINFO".toArg(), "GROUPS".toArg(), key.toArg()),
).unwrapList<RType>()

suspend fun ReThis.xInfoStream(
    key: String,
    full: Boolean = false,
    limit: XOption.Limit? = null,
): Map<String, RType?> = execute(
    mutableListOf(
        "XINFO".toArg(),
        "STREAM".toArg(),
        key.toArg(),
    ).writeArg(
        full.takeIf { it }?.let { "FULL" },
        limit,
    ),
).unwrapRespIndMap<String, RType>() ?: emptyMap()

suspend fun ReThis.xLen(key: String): Long? = execute<Long>(listOf("XLEN".toArg(), key.toArg()))

suspend fun ReThis.xPending(key: String, group: String, option: XPendingOption? = null): List<RType> = execute(
    mutableListOf("XPENDING".toArg(), key.toArg(), group.toArg()).writeArg(option),
).unwrapList<RType>()

suspend fun ReThis.xRange(key: String, start: String, end: String, limit: XOption.Limit? = null): List<RType> = execute(
    mutableListOf("XRANGE".toArg(), key.toArg(), start.toArg(), end.toArg()).writeArg(limit),
).unwrapList<RType>()

suspend fun ReThis.xRead(
    keys: List<String>,
    ids: List<String>,
    count: XOption.Limit? = null,
    blockMillis: Long? = null,
): Map<String, RType?>? = execute(
    mutableListOf<Argument>(
        "XREAD".toArg(),
    ).writeArg(
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
        "XREADGROUP".toArg(),
        "GROUP".toArg(),
        group.toArg(),
        consumer.toArg(),
    ).writeArg(
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
    mutableListOf("XREVRANGE".toArg(), key.toArg(), end.toArg(), start.toArg()).writeArg(limit),
).unwrapList<RType>()

suspend fun ReThis.xSetId(
    key: String,
    lastId: String,
    entriesAdded: Long? = null,
    maxDeletedId: String? = null,
): Boolean = execute<String>(
    mutableListOf(
        "XSETID".toArg(),
        key.toArg(),
        lastId.toArg(),
    ).writeArg(
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
        "XTRIM".toArg(),
        key.toArg(),
    ).writeArg(
        trimmingStrategy.toArg(),
        exactement,
        threshold,
        trim,
    ),
)
