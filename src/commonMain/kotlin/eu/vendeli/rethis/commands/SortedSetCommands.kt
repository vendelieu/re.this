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

suspend fun ReThis.bzMPop(
    timeout: Double,
    minMax: ZPopCommonOption,
    vararg keys: String,
    count: Long? = null,
): List<MPopResult> = execute(
    listOfNotNull(
        "BZMPOP",
        timeout,
        keys.size,
        *keys,
        minMax,
        count?.let { "COUNT" to it },
    ),
).unwrapList<RType>().chunked(2) { item ->
    MPopResult(name = item.first().unwrap<String>()!!, poppedElements = item.last().unwrapSet<String>().toList())
}

suspend fun ReThis.bzPopMax(
    timeout: Double,
    vararg keys: String,
): ZPopResult? = execute(
    listOf(
        "BZPOPMAX",
        *keys,
        timeout,
    ),
).unwrapList<RType>().takeIf { it.size == 3 }?.let {
    ZPopResult(key = it[0].unwrap()!!, popped = it[1].unwrap()!!, score = it[2].unwrap()!!)
}

suspend fun ReThis.bzPopMin(
    timeout: Double,
    vararg keys: String,
): ZPopResult? = execute(
    listOf(
        "BZPOPMIN",
        *keys,
        timeout,
    ),
).unwrapList<RType>().takeIf { it.size == 3 }?.let {
    ZPopResult(key = it[0].unwrap()!!, popped = it[1].unwrap()!!, score = it[2].unwrap()!!)
}

suspend fun ReThis.zAdd(
    key: String,
    updateType: UpdateStrategyOption? = null,
    ch: Boolean = false,
    vararg members: ZMember,
): Long = execute(
    listOfNotNull(
        "ZADD",
        key,
        updateType,
        ch.takeIf { it }?.let { "CH" },
        *members.map { it.score to it.member }.toTypedArray(),
    ),
).unwrap() ?: 0

suspend fun ReThis.zAdd(
    key: String,
    vararg members: ZMember,
    existenceRule: UpdateStrategyOption.ExistenceRule? = null,
    comparisonRule: UpdateStrategyOption.ComparisonRule? = null,
    ch: Boolean = false,
    incr: Boolean = false,
): Long = execute(
    listOfNotNull(
        "ZADD",
        key,
        existenceRule,
        comparisonRule,
        ch.takeIf { it }?.let { "CH" },
        incr.takeIf { it }?.let { "INCR" },
        *members.map { it.score to it.member }.toTypedArray(),
    ),
).unwrap() ?: 0

suspend fun ReThis.zCard(key: String): Long = execute(
    listOf(
        "ZCARD",
        key,
    ),
).unwrap() ?: 0

suspend fun ReThis.zCount(key: String, min: Double, max: Double): Long = execute(
    listOf(
        "ZCOUNT",
        key,
        min,
        max,
    ),
).unwrap() ?: 0

suspend fun ReThis.zDiff(vararg keys: String, withScores: Boolean = false): List<String> = execute(
    listOfNotNull(
        "ZDIFF",
        keys.size,
        *keys,
        withScores.takeIf { it }?.let { "WITHSCORES" },
    ),
).unwrapList()

suspend fun ReThis.zDiffStore(destination: String, vararg keys: String): Long = execute(
    listOf(
        "ZDIFFSTORE",
        destination,
        keys.size,
        *keys,
    ),
).unwrap() ?: 0

suspend fun ReThis.zIncrby(key: String, member: String, increment: Double): Double = execute(
    listOf(
        "ZINCRBY",
        key,
        increment,
        member,
    ),
).unwrap() ?: 0.0

suspend fun ReThis.zInter(
    vararg keys: String,
    weights: List<Long>? = null,
    aggregate: ZAggregate? = null,
    withScores: Boolean = false,
): List<String> =
    execute(
        listOfNotNull(
            "ZINTER",
            keys.size,
            *keys,
            weights?.let { "WEIGHTS" to it },
            aggregate?.let { "AGGREGATE" to it },
            withScores.takeIf { it }?.let { "WITHSCORES" },
        ),
    ).unwrapList()

suspend fun ReThis.zInterCard(vararg keys: String, limit: Long? = null): Long = execute(
    listOfNotNull(
        "ZINTERCARD",
        keys.size,
        *keys,
        limit?.let { "LIMIT" to it },
    ),
).unwrap() ?: 0

suspend fun ReThis.zInterStore(
    destination: String,
    vararg keys: String,
    weights: List<Long>? = null,
    aggregate: ZAggregate? = null,
): Long = execute(
    listOfNotNull(
        "ZINTERSTORE",
        destination,
        keys.size,
        *keys,
        weights?.let { "WEIGHTS" to it },
        aggregate?.let { "AGGREGATE" to it },
    ),
).unwrap() ?: 0

suspend fun ReThis.zLexCount(key: String, min: String, max: String): Long = execute(
    listOf(
        "ZLEXCOUNT",
        key,
        min,
        max,
    ),
).unwrap() ?: 0

suspend fun ReThis.zMpop(
    modifier: ZPopCommonOption,
    vararg keys: String,
    count: Long? = null,
): List<MPopResult> = execute(
    listOfNotNull(
        "ZMPOP",
        keys.size,
        *keys,
        modifier,
        count?.let { "COUNT" to it },
    ),
).unwrapList<RType>().chunked(2) { item ->
    MPopResult(name = item.first().unwrap<String>()!!, poppedElements = item.last().unwrapSet<String>().toList())
}

suspend fun ReThis.zMscore(key: String, vararg members: String): List<Double?> = execute(
    listOf(
        "ZMSCORE",
        key,
        *members,
    ),
).unwrapList<Double?>()

suspend fun ReThis.zPopmax(key: String, count: Long? = null): List<MPopResult> = execute(
    listOfNotNull(
        "ZPOPMAX",
        key,
        count,
    ),
).unwrapList<RType>().chunked(2) { item ->
    MPopResult(name = item.first().unwrap<String>()!!, poppedElements = item.last().unwrapSet<String>().toList())
}

suspend fun ReThis.zPopmin(key: String): List<Double> = execute(
    listOfNotNull(
        "ZPOPMIN",
        key,
    ),
).unwrapList<Double>()

suspend fun ReThis.zPopmin(key: String, count: Long): List<List<ZMember>> = execute(
    listOfNotNull(
        "ZPOPMIN",
        key,
        count,
    ),
).cast<RArray>().unwrapList<RType>().map {
    it.cast<RArray>().value.chunked(2) { i ->
        ZMember(i.first().unwrap()!!, i.last().unwrap()!!)
    }
}

suspend fun ReThis.zRandmember(key: String): String? = execute(
    listOf(
        "ZRANDMEMBER",
        key,
    ),
).unwrap()

suspend fun ReThis.zRandmember(
    key: String,
    count: Long,
): List<String> = execute(
    listOfNotNull(
        "ZRANDMEMBER",
        key,
        count,
    ),
).unwrapList()

suspend fun ReThis.zRandmember(
    key: String,
    count: Long,
    withScores: Boolean = false,
): List<List<ZMember>> = execute(
    listOfNotNull(
        "ZRANDMEMBER",
        key,
        count,
        withScores.takeIf { it }?.let { "WITHSCORES" },
    ),
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
    listOfNotNull(
        "ZRANGE",
        key,
        start,
        stop,
        type,
        rev.takeIf { it }?.let { "REV" },
        limit,
        withScores.takeIf { it }?.let { "WITHSCORES" },
    ),
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
    listOfNotNull(
        "ZRANGESTORE",
        destination,
        src,
        min,
        max,
        rangeType,
        rev.takeIf { it }?.let { "REV" },
        if (offset != null && count != null) Triple("LIMIT", offset, count) else null,
    ),
).unwrap() ?: 0

suspend fun ReThis.zRank(key: String, member: String): Long? = execute(
    listOf(
        "ZRANK",
        key,
        member,
    ),
).unwrap<Long?>()

suspend fun ReThis.zRem(key: String, vararg members: String): Long = execute(
    listOf(
        "ZREM",
        key,
        *members,
    ),
).unwrap() ?: 0

suspend fun ReThis.zRemRangeByLex(key: String, min: String, max: String): Long = execute(
    listOf(
        "ZREMRANGEBYLEX",
        key,
        min,
        max,
    ),
).unwrap() ?: 0

suspend fun ReThis.zRemRangeByRank(key: String, start: Long, stop: Long): Long = execute(
    listOf(
        "ZREMRANGEBYRANK",
        key,
        start,
        stop,
    ),
).unwrap() ?: 0

suspend fun ReThis.zRemRangeByScore(key: String, min: Double, max: Double): Long = execute(
    listOf(
        "ZREMRANGEBYSCORE",
        key,
        min,
        max,
    ),
).unwrap() ?: 0

suspend fun ReThis.zRevrank(key: String, member: String): Long? = execute(
    listOf(
        "ZREVRANK",
        key,
        member,
    ),
).unwrap<Long?>()

suspend fun ReThis.zScan(
    key: String,
    cursor: Long,
    pattern: String? = null,
    count: Long? = null,
): ScanResult<Pair<String, String>> {
    val response = execute(
        listOfNotNull(
            "ZSCAN",
            key,
            cursor,
            pattern,
            count,
        ),
    )
    val arrResponse = response.safeCast<RArray>()?.value ?: exception { "Wrong response type" }
    val newCursor = arrResponse[0].unwrap<String>() ?: exception { "Missing cursor in response" }

    val keysArray = arrResponse[1].safeCast<RArray>()?.value ?: exception { "Missing keys in response" }
    val keys = keysArray.chunked(2) { it.first().unwrap<String>()!! to it.last().unwrap<String>()!! }

    return ScanResult(cursor = newCursor, keys = keys)
}

suspend fun ReThis.zScore(key: String, member: String): Double? = execute(
    listOf(
        "ZSCORE",
        key,
        member,
    ),
).unwrap<Double?>()

suspend fun ReThis.zUnion(
    vararg keys: String,
    weights: List<Long>? = null,
    aggregate: ZAggregate? = null,
    withScores: Boolean = false,
): List<String> = execute(
    listOfNotNull(
        "ZUNION",
        keys.size,
        *keys,
        weights?.let { "WEIGHTS" to it },
        aggregate?.let { "AGGREGATE" to it },
        withScores.takeIf { it }?.let { "WITHSCORES" },
    ),
).unwrapList()

suspend fun ReThis.zUnionStore(
    destination: String,
    vararg keys: String,
    weights: List<Long>? = null,
    aggregate: ZAggregate? = null,
): Long = execute(
    listOfNotNull(
        "ZUNIONSTORE",
        destination,
        keys.size,
        *keys,
        weights,
        aggregate,
    ),
).unwrap() ?: 0
