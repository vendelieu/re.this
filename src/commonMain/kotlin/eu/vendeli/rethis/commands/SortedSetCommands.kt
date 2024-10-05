package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.exception
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
import eu.vendeli.rethis.utils.safeCast
import eu.vendeli.rethis.utils.writeArg

suspend fun ReThis.bzMPop(
    timeout: Double,
    minMax: ZPopCommonOption,
    vararg keys: String,
    count: Long? = null,
): List<MPopResult> = execute(
    mutableListOf(
        "BZMPOP".toArg(),
    ).apply {
        writeArg(timeout)
        writeArg(keys.size)
        keys.forEach { writeArg(it) }
        writeArg(minMax)
        count?.let { writeArg("COUNT" to it) }
    },
).unwrapList<RType>().chunked(2) { item ->
    MPopResult(name = item.first().unwrap<String>()!!, poppedElements = item.last().unwrapSet<String>().toList())
}

suspend fun ReThis.bzPopMax(
    timeout: Double,
    vararg keys: String,
): ZPopResult? = execute(
    mutableListOf(
        "BZPOPMAX".toArg(),
    ).apply {
        keys.forEach { writeArg(it) }
        writeArg(timeout)
    },
).unwrapList<RType>().takeIf { it.size == 3 }?.let {
    ZPopResult(key = it[0].unwrap()!!, popped = it[1].unwrap()!!, score = it[2].unwrap()!!)
}

suspend fun ReThis.bzPopMin(
    timeout: Double,
    vararg keys: String,
): ZPopResult? = execute(
    mutableListOf(
        "BZPOPMIN".toArg(),
    ).apply {
        keys.forEach { writeArg(it) }
        writeArg(timeout)
    },
).unwrapList<RType>().takeIf { it.size == 3 }?.let {
    ZPopResult(key = it[0].unwrap()!!, popped = it[1].unwrap()!!, score = it[2].unwrap()!!)
}

suspend fun ReThis.zAdd(
    key: String,
    updateType: UpdateStrategyOption? = null,
    ch: Boolean = false,
    vararg members: ZMember,
): Long = execute(
    mutableListOf(
        "ZADD".toArg(),
        key.toArg(),
    ).apply {
        writeArg(updateType)
        if (ch) writeArg("CH")
        members.forEach { writeArg(it.score to it.member) }
    },
).unwrap() ?: 0

suspend fun ReThis.zAdd(
    key: String,
    vararg members: ZMember,
    existenceRule: UpdateStrategyOption.ExistenceRule? = null,
    comparisonRule: UpdateStrategyOption.ComparisonRule? = null,
    ch: Boolean = false,
    incr: Boolean = false,
): Long = execute(
    mutableListOf(
        "ZADD".toArg(),
        key.toArg(),
    ).apply {
        writeArg(existenceRule)
        writeArg(comparisonRule)
        if (ch) writeArg("CH")
        if (incr) writeArg("INCR")
        members.forEach { writeArg(it.score to it.member) }
    },
).unwrap() ?: 0

suspend fun ReThis.zCard(key: String): Long = execute(
    listOf(
        "ZCARD".toArg(),
        key.toArg(),
    ),
).unwrap() ?: 0

suspend fun ReThis.zCount(key: String, min: Double, max: Double): Long = execute(
    listOf(
        "ZCOUNT".toArg(),
        key.toArg(),
        min.toArg(),
        max.toArg(),
    ),
).unwrap() ?: 0

suspend fun ReThis.zDiff(vararg keys: String, withScores: Boolean = false): List<String> = execute(
    mutableListOf(
        "ZDIFF".toArg(),
    ).apply {
        writeArg(keys.size)
        writeArg(keys)
        if (withScores) writeArg("WITHSCORES")
    },
).unwrapList()

suspend fun ReThis.zDiffStore(destination: String, vararg keys: String): Long = execute(
    mutableListOf(
        "ZDIFFSTORE".toArg(),
        destination.toArg(),
    ).apply {
        writeArg(keys.size)
        writeArg(keys)
    },
).unwrap() ?: 0

suspend fun ReThis.zIncrby(key: String, member: String, increment: Double): Double = execute(
    listOf(
        "ZINCRBY".toArg(),
        key.toArg(),
        increment.toArg(),
        member.toArg(),
    ),
).unwrap() ?: 0.0

suspend fun ReThis.zInter(
    vararg keys: String,
    weights: List<Long>? = null,
    aggregate: ZAggregate? = null,
    withScores: Boolean = false,
): List<String> = execute(
    mutableListOf(
        "ZINTER".toArg(),
        keys.size.toArg(),
        *keys.toArg(),
    ).apply {
        weights?.let { writeArg("WEIGHTS" to it) }
        aggregate?.let { writeArg("AGGREGATE" to it) }
        if (withScores) writeArg("WITHSCORES")
    },
).unwrapList()

suspend fun ReThis.zInterCard(vararg keys: String, limit: Long? = null): Long = execute(
    mutableListOf(
        "ZINTERCARD".toArg(),
        keys.size.toArg(),
        *keys.toArg(),
    ).apply {
        limit?.let { writeArg("LIMIT" to it) }
    },
).unwrap() ?: 0

suspend fun ReThis.zInterStore(
    destination: String,
    vararg keys: String,
    weights: List<Long>? = null,
    aggregate: ZAggregate? = null,
): Long = execute(
    mutableListOf(
        "ZINTERSTORE".toArg(),
        destination.toArg(),
        keys.size.toArg(),
        *keys.toArg(),
    ).apply {
        writeArg(weights)
        writeArg(aggregate)
    },
).unwrap() ?: 0

suspend fun ReThis.zLexCount(key: String, min: String, max: String): Long = execute(
    listOf(
        "ZLEXCOUNT".toArg(),
        key.toArg(),
        min.toArg(),
        max.toArg(),
    ),
).unwrap() ?: 0

suspend fun ReThis.zMpop(
    modifier: ZPopCommonOption,
    vararg keys: String,
    count: Long? = null,
): List<MPopResult> = execute(
    mutableListOf(
        "ZMPOP".toArg(),
        keys.size.toArg(),
        *keys.toArg(),
    ).apply {
        writeArg(modifier)
        count?.let { writeArg("COUNT" to it) }
    },
).unwrapList<RType>().chunked(2) { item ->
    MPopResult(name = item.first().unwrap<String>()!!, poppedElements = item.last().unwrapSet<String>().toList())
}

suspend fun ReThis.zMscore(key: String, vararg members: String): List<Double?> = execute(
    listOf(
        "ZMSCORE".toArg(),
        key.toArg(),
        *members.toArg(),
    ),
).unwrapList<Double?>()

suspend fun ReThis.zPopmax(key: String, count: Long? = null): List<MPopResult> = execute(
    mutableListOf(
        "ZPOPMAX".toArg(),
        key.toArg(),
    ).writeArg(count),
).unwrapList<RType>().chunked(2) { item ->
    MPopResult(name = item.first().unwrap<String>()!!, poppedElements = item.last().unwrapSet<String>().toList())
}

suspend fun ReThis.zPopmin(key: String): List<Double> = execute(
    listOfNotNull(
        "ZPOPMIN".toArg(),
        key.toArg(),
    ),
).unwrapList<Double>()

suspend fun ReThis.zPopmin(key: String, count: Long): List<List<ZMember>> = execute(
    listOfNotNull(
        "ZPOPMIN".toArg(),
        key.toArg(),
        count.toArg(),
    ),
).cast<RArray>().unwrapList<RType>().map {
    it.cast<RArray>().value.chunked(2) { i ->
        ZMember(i.first().unwrap()!!, i.last().unwrap()!!)
    }
}

suspend fun ReThis.zRandmember(key: String): String? = execute(
    listOf(
        "ZRANDMEMBER".toArg(),
        key.toArg(),
    ),
).unwrap()

suspend fun ReThis.zRandmember(
    key: String,
    count: Long,
): List<String> = execute(
    listOfNotNull(
        "ZRANDMEMBER".toArg(),
        key.toArg(),
        count.toArg(),
    ),
).unwrapList()

suspend fun ReThis.zRandmember(
    key: String,
    count: Long,
    withScores: Boolean = false,
): List<List<ZMember>> = execute(
    mutableListOf(
        "ZRANDMEMBER".toArg(),
        key.toArg(),
        count.toArg(),
    ).apply {
        if (withScores) writeArg("WITHSCORES")
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
        "ZRANGE".toArg(),
        key.toArg(),
        start.toArg(),
        stop.toArg(),
    ).apply {
        writeArg(type)
        if (rev) writeArg("REV")
        writeArg(limit)
        if (withScores) writeArg("WITHSCORES")
    },
).unwrapList()

suspend fun ReThis.zRangeStore(
    destination: String,
    src: String,
    min: Long,
    max: Long,
    rangeType: ZRangeOption.Type? = null,
    rev: Boolean = false,
    offset: Long? = null,
    count: Long? = null,
): Long = execute(
    mutableListOf(
        "ZRANGESTORE".toArg(),
        destination.toArg(),
        src.toArg(),
        min.toArg(),
        max.toArg(),
    ).apply {
        writeArg(rangeType)
        if (rev) writeArg("REV")
        if (offset != null && count != null) {
            writeArg("LIMIT")
            writeArg(offset to count)
        }
    },
).unwrap() ?: 0

suspend fun ReThis.zRank(key: String, member: String): Long? = execute(
    listOf(
        "ZRANK".toArg(),
        key.toArg(),
        member.toArg(),
    ),
).unwrap<Long?>()

suspend fun ReThis.zRem(key: String, vararg members: String): Long = execute(
    listOf(
        "ZREM".toArg(),
        key.toArg(),
        *members.toArg(),
    ),
).unwrap() ?: 0

suspend fun ReThis.zRemRangeByLex(key: String, min: String, max: String): Long = execute(
    listOf(
        "ZREMRANGEBYLEX".toArg(),
        key.toArg(),
        min.toArg(),
        max.toArg(),
    ),
).unwrap() ?: 0

suspend fun ReThis.zRemRangeByRank(key: String, start: Long, stop: Long): Long = execute(
    listOf(
        "ZREMRANGEBYRANK".toArg(),
        key.toArg(),
        start.toArg(),
        stop.toArg(),
    ),
).unwrap() ?: 0

suspend fun ReThis.zRemRangeByScore(key: String, min: Double, max: Double): Long = execute(
    listOf(
        "ZREMRANGEBYSCORE".toArg(),
        key.toArg(),
        min.toArg(),
        max.toArg(),
    ),
).unwrap() ?: 0

suspend fun ReThis.zRevrank(key: String, member: String): Long? = execute(
    listOf(
        "ZREVRANK".toArg(),
        key.toArg(),
        member.toArg(),
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
            "ZSCAN".toArg(),
            key.toArg(),
            cursor.toArg(),
        ).apply {
            writeArg(pattern)
            writeArg(count)
        },
    )
    val arrResponse = response.safeCast<RArray>()?.value ?: exception { "Wrong response type" }
    val newCursor = arrResponse[0].unwrap<String>() ?: exception { "Missing cursor in response" }

    val keysArray = arrResponse[1].safeCast<RArray>()?.value ?: exception { "Missing keys in response" }
    val keys = keysArray.chunked(2) { it.first().unwrap<String>()!! to it.last().unwrap<String>()!! }

    return ScanResult(cursor = newCursor, keys = keys)
}

suspend fun ReThis.zScore(key: String, member: String): Double? = execute(
    listOf(
        "ZSCORE".toArg(),
        key.toArg(),
        member.toArg(),
    ),
).unwrap<Double?>()

suspend fun ReThis.zUnion(
    vararg keys: String,
    weights: List<Long>? = null,
    aggregate: ZAggregate? = null,
    withScores: Boolean = false,
): List<String> = execute(
    mutableListOf(
        "ZUNION".toArg(),
        keys.size.toArg(),
        *keys.toArg(),
    ).apply {
        weights?.let { writeArg("WEIGHTS" to it) }
        aggregate?.let { writeArg("AGGREGATE" to it) }
        if (withScores) writeArg("WITHSCORES")
    },
).unwrapList()

suspend fun ReThis.zUnionStore(
    destination: String,
    vararg keys: String,
    weights: List<Long>? = null,
    aggregate: ZAggregate? = null,
): Long = execute(
    mutableListOf(
        "ZUNIONSTORE".toArg(),
        destination.toArg(),
        keys.size.toArg(),
        *keys.toArg(),
    ).apply {
        writeArg(weights)
        writeArg(aggregate)
    },
).unwrap() ?: 0
