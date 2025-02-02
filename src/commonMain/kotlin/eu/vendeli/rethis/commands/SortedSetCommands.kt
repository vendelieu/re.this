package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.processingException
import eu.vendeli.rethis.types.common.MPopResult
import eu.vendeli.rethis.types.common.ScanResult
import eu.vendeli.rethis.types.common.ZMember
import eu.vendeli.rethis.types.common.ZPopResult
import eu.vendeli.rethis.types.core.*
import eu.vendeli.rethis.types.options.UpdateStrategyOption
import eu.vendeli.rethis.types.options.ZAggregate
import eu.vendeli.rethis.types.options.ZPopCommonOption
import eu.vendeli.rethis.types.options.ZRangeOption
import eu.vendeli.rethis.utils.cast
import eu.vendeli.rethis.utils.response.unwrapRespIndMap
import eu.vendeli.rethis.utils.safeCast
import eu.vendeli.rethis.utils.writeArgument
import eu.vendeli.rethis.utils.execute

suspend fun ReThis.bzMPop(
    timeout: Double,
    minMax: ZPopCommonOption,
    vararg keys: String,
    count: Long? = null,
): List<MPopResult> = execute(
    mutableListOf(
        "BZMPOP".toArgument(),
    ).apply {
        writeArgument(timeout)
        writeArgument(keys.size)
        keys.forEach { writeArgument(it) }
        writeArgument(minMax)
        count?.let { writeArgument("COUNT" to it) }
    },
).unwrapList<RType>().chunked(2) { item ->
    MPopResult(name = item.first().unwrap<String>()!!, poppedElements = item.last().unwrapSet<String>().toList())
}

suspend fun ReThis.bzPopMax(
    timeout: Double,
    vararg keys: String,
): ZPopResult? = execute(
    mutableListOf(
        "BZPOPMAX".toArgument(),
    ).apply {
        keys.forEach { writeArgument(it) }
        writeArgument(timeout)
    },
).unwrapList<RType>().takeIf { it.size == 3 }?.let {
    ZPopResult(key = it[0].unwrap()!!, popped = it[1].unwrap()!!, score = it[2].unwrap()!!)
}

suspend fun ReThis.bzPopMin(
    timeout: Double,
    vararg keys: String,
): ZPopResult? = execute(
    mutableListOf(
        "BZPOPMIN".toArgument(),
    ).apply {
        keys.forEach { writeArgument(it) }
        writeArgument(timeout)
    },
).unwrapList<RType>().takeIf { it.size == 3 }?.let {
    ZPopResult(key = it[0].unwrap()!!, popped = it[1].unwrap()!!, score = it[2].unwrap()!!)
}

suspend fun ReThis.zAdd(
    key: String,
    updateType: UpdateStrategyOption? = null,
    ch: Boolean = false,
    vararg members: ZMember,
): Long = execute<Long>(
    mutableListOf(
        "ZADD".toArgument(),
        key.toArgument(),
    ).apply {
        writeArgument(updateType)
        if (ch) writeArgument("CH")
        members.forEach { writeArgument(it.score to it.member) }
    },
) ?: 0

suspend fun ReThis.zAdd(
    key: String,
    vararg members: ZMember,
    existenceRule: UpdateStrategyOption.ExistenceRule? = null,
    comparisonRule: UpdateStrategyOption.ComparisonRule? = null,
    ch: Boolean = false,
    incr: Boolean = false,
): Long = execute<Long>(
    mutableListOf(
        "ZADD".toArgument(),
        key.toArgument(),
    ).apply {
        writeArgument(existenceRule)
        writeArgument(comparisonRule)
        if (ch) writeArgument("CH")
        if (incr) writeArgument("INCR")
        members.forEach { writeArgument(it.score to it.member) }
    },
) ?: 0

suspend fun ReThis.zCard(key: String): Long = execute<Long>(
    listOf(
        "ZCARD".toArgument(),
        key.toArgument(),
    ),
) ?: 0

suspend fun ReThis.zCount(key: String, min: Double, max: Double): Long = execute<Long>(
    listOf(
        "ZCOUNT".toArgument(),
        key.toArgument(),
        min.toArgument(),
        max.toArgument(),
    ),
) ?: 0

suspend fun ReThis.zDiff(vararg keys: String, withScores: Boolean = false): List<String> = execute(
    mutableListOf(
        "ZDIFF".toArgument(),
    ).apply {
        writeArgument(keys.size)
        writeArgument(keys)
        if (withScores) writeArgument("WITHSCORES")
    },
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.zDiffStore(destination: String, vararg keys: String): Long = execute<Long>(
    mutableListOf(
        "ZDIFFSTORE".toArgument(),
        destination.toArgument(),
    ).apply {
        writeArgument(keys.size)
        writeArgument(keys)
    },
) ?: 0

suspend fun ReThis.zIncrBy(key: String, member: String, increment: Double): Double = execute<Double>(
    listOf(
        "ZINCRBY".toArgument(),
        key.toArgument(),
        increment.toArgument(),
        member.toArgument(),
    ),
) ?: 0.0

suspend fun ReThis.zInter(
    vararg keys: String,
    weights: List<Long>? = null,
    aggregate: ZAggregate? = null,
    withScores: Boolean = false,
): List<String> = execute(
    mutableListOf(
        "ZINTER".toArgument(),
        keys.size.toArgument(),
        *keys.toArgument(),
    ).apply {
        weights?.let { writeArgument("WEIGHTS" to it) }
        aggregate?.let { writeArgument("AGGREGATE" to it) }
        if (withScores) writeArgument("WITHSCORES")
    },
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.zInterCard(vararg keys: String, limit: Long? = null): Long = execute<Long>(
    mutableListOf(
        "ZINTERCARD".toArgument(),
        keys.size.toArgument(),
        *keys.toArgument(),
    ).apply {
        limit?.let { writeArgument("LIMIT" to it) }
    },
) ?: 0

suspend fun ReThis.zInterStore(
    destination: String,
    vararg keys: String,
    weights: List<Long>? = null,
    aggregate: ZAggregate? = null,
): Long = execute<Long>(
    mutableListOf(
        "ZINTERSTORE".toArgument(),
        destination.toArgument(),
        keys.size.toArgument(),
        *keys.toArgument(),
    ).apply {
        writeArgument(weights)
        writeArgument(aggregate)
    },
) ?: 0

suspend fun ReThis.zLexCount(key: String, min: String, max: String): Long = execute<Long>(
    listOf(
        "ZLEXCOUNT".toArgument(),
        key.toArgument(),
        min.toArgument(),
        max.toArgument(),
    ),
) ?: 0

suspend fun ReThis.zMPop(
    modifier: ZPopCommonOption,
    vararg keys: String,
    count: Long? = null,
): List<MPopResult> = execute(
    mutableListOf(
        "ZMPOP".toArgument(),
        keys.size.toArgument(),
        *keys.toArgument(),
    ).apply {
        writeArgument(modifier)
        count?.let { writeArgument("COUNT" to it) }
    },
).unwrapList<RType>().chunked(2) { item ->
    MPopResult(name = item.first().unwrap<String>()!!, poppedElements = item.last().unwrapSet<String>().toList())
}

suspend fun ReThis.zMScore(key: String, vararg members: String): List<Double?> = execute(
    listOf(
        "ZMSCORE".toArgument(),
        key.toArgument(),
        *members.toArgument(),
    ),
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.zPopMax(key: String, count: Long? = null): List<MPopResult> = execute(
    mutableListOf(
        "ZPOPMAX".toArgument(),
        key.toArgument(),
    ).writeArgument(count),
).unwrapList<RType>().chunked(2) { item ->
    MPopResult(name = item.first().unwrap<String>()!!, poppedElements = item.last().unwrapSet<String>().toList())
}

suspend fun ReThis.zPopMin(key: String): Map<String, Double?> = execute(
    listOfNotNull(
        "ZPOPMIN".toArgument(),
        key.toArgument(),
    ),
).unwrapRespIndMap<String, Double>() ?: emptyMap()

suspend fun ReThis.zPopMin(key: String, count: Long): List<List<ZMember>> = execute(
    listOfNotNull(
        "ZPOPMIN".toArgument(),
        key.toArgument(),
        count.toArgument(),
    ),
).cast<RArray>().unwrapList<RType>().map {
    it.cast<RArray>().value.chunked(2) { i ->
        ZMember(i.first().unwrap()!!, i.last().unwrap()!!)
    }
}

suspend fun ReThis.zRandMember(key: String): String? = execute<String>(
    listOf(
        "ZRANDMEMBER".toArgument(),
        key.toArgument(),
    ),
)

suspend fun ReThis.zRandMember(
    key: String,
    count: Long,
): List<String> = execute<String>(
    listOfNotNull(
        "ZRANDMEMBER".toArgument(),
        key.toArgument(),
        count.toArgument(),
    ),
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.zRandMember(
    key: String,
    count: Long,
    withScores: Boolean = false,
): List<List<ZMember>> = execute(
    mutableListOf(
        "ZRANDMEMBER".toArgument(),
        key.toArgument(),
        count.toArgument(),
    ).apply {
        if (withScores) writeArgument("WITHSCORES")
    },
).cast<RArray>().unwrapList<RType>().map {
    it.cast<RArray>().value.chunked(2) { i ->
        ZMember(i.first().unwrap()!!, i.last().unwrap()!!)
    }
}

suspend fun ReThis.zRange(
    key: String,
    start: Long,
    stop: Long,
    type: ZRangeOption.Type? = null,
    rev: Boolean = true,
    limit: ZRangeOption.LIMIT? = null,
    withScores: Boolean = false,
): List<String> = execute(
    mutableListOf(
        "ZRANGE".toArgument(),
        key.toArgument(),
        start.toArgument(),
        stop.toArgument(),
    ).apply {
        writeArgument(type)
        if (rev) writeArgument("REV")
        writeArgument(limit)
        if (withScores) writeArgument("WITHSCORES")
    },
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.zRangeStore(
    destination: String,
    src: String,
    min: Long,
    max: Long,
    rangeType: ZRangeOption.Type? = null,
    rev: Boolean = false,
    offset: Long? = null,
    count: Long? = null,
): Long = execute<Long>(
    mutableListOf(
        "ZRANGESTORE".toArgument(),
        destination.toArgument(),
        src.toArgument(),
        min.toArgument(),
        max.toArgument(),
    ).apply {
        writeArgument(rangeType)
        if (rev) writeArgument("REV")
        if (offset != null && count != null) {
            writeArgument("LIMIT")
            writeArgument(offset to count)
        }
    },
) ?: 0

suspend fun ReThis.zRank(key: String, member: String): Long? = execute(
    listOf(
        "ZRANK".toArgument(),
        key.toArgument(),
        member.toArgument(),
    ),
).unwrap<Long?>()

suspend fun ReThis.zRem(key: String, vararg members: String): Long = execute<Long>(
    listOf(
        "ZREM".toArgument(),
        key.toArgument(),
        *members.toArgument(),
    ),
) ?: 0

suspend fun ReThis.zRemRangeByLex(key: String, min: String, max: String): Long = execute<Long>(
    listOf(
        "ZREMRANGEBYLEX".toArgument(),
        key.toArgument(),
        min.toArgument(),
        max.toArgument(),
    ),
) ?: 0

suspend fun ReThis.zRemRangeByRank(key: String, start: Long, stop: Long): Long = execute<Long>(
    listOf(
        "ZREMRANGEBYRANK".toArgument(),
        key.toArgument(),
        start.toArgument(),
        stop.toArgument(),
    ),
) ?: 0

suspend fun ReThis.zRemRangeByScore(key: String, min: Double, max: Double): Long = execute<Long>(
    listOf(
        "ZREMRANGEBYSCORE".toArgument(),
        key.toArgument(),
        min.toArgument(),
        max.toArgument(),
    ),
) ?: 0

suspend fun ReThis.zRevRank(key: String, member: String): Long? = execute(
    listOf(
        "ZREVRANK".toArgument(),
        key.toArgument(),
        member.toArgument(),
    ),
).unwrap<Long?>()

suspend fun ReThis.zScan(
    key: String,
    cursor: Long,
    pattern: String? = null,
    count: Long? = null,
): ScanResult<Pair<String, String>> {
    val response = execute(
        mutableListOf(
            "ZSCAN".toArgument(),
            key.toArgument(),
            cursor.toArgument(),
        ).apply {
            writeArgument(pattern)
            writeArgument(count)
        },
    )
    val arrResponse = response.safeCast<RArray>()?.value ?: processingException { "Wrong response type" }
    val newCursor = arrResponse[0].unwrap<String>() ?: processingException { "Missing cursor in response" }

    val keysArray = arrResponse[1].safeCast<RArray>()?.value ?: processingException { "Missing keys in response" }
    val keys = keysArray.chunked(2) { it.first().unwrap<String>()!! to it.last().unwrap<String>()!! }

    return ScanResult(cursor = newCursor, keys = keys)
}

suspend fun ReThis.zScore(key: String, member: String): Double? = execute(
    listOf(
        "ZSCORE".toArgument(),
        key.toArgument(),
        member.toArgument(),
    ),
).unwrap<Double?>()

suspend fun ReThis.zUnion(
    vararg keys: String,
    weights: List<Long>? = null,
    aggregate: ZAggregate? = null,
    withScores: Boolean = false,
): List<String> = execute(
    mutableListOf(
        "ZUNION".toArgument(),
        keys.size.toArgument(),
        *keys.toArgument(),
    ).apply {
        weights?.let { writeArgument("WEIGHTS" to it) }
        aggregate?.let { writeArgument("AGGREGATE" to it) }
        if (withScores) writeArgument("WITHSCORES")
    },
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.zUnionStore(
    destination: String,
    vararg keys: String,
    weights: List<Long>? = null,
    aggregate: ZAggregate? = null,
): Long = execute<Long>(
    mutableListOf(
        "ZUNIONSTORE".toArgument(),
        destination.toArgument(),
        keys.size.toArgument(),
        *keys.toArgument(),
    ).apply {
        writeArgument(weights)
        writeArgument(aggregate)
    },
) ?: 0
