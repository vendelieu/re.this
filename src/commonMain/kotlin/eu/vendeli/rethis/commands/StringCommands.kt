package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.types.common.LcsResult
import eu.vendeli.rethis.exception
import eu.vendeli.rethis.types.options.*
import eu.vendeli.rethis.types.core.*
import eu.vendeli.rethis.types.core.unwrap
import eu.vendeli.rethis.utils.cast
import eu.vendeli.rethis.utils.safeCast

suspend fun ReThis.append(key: String, value: String): Long = execute(
    listOf(
        "APPEND",
        key,
        value,
    ),
).unwrap() ?: 0

suspend fun ReThis.decr(key: String): Long = execute(
    listOf(
        "DECR",
        key,
    ),
).unwrap() ?: 0

suspend fun ReThis.decrBy(key: String, decrement: Long): Long = execute(
    listOf(
        "DECRBY",
        key,
        decrement,
    ),
).unwrap() ?: 0

suspend fun ReThis.get(key: String): String? = execute(
    listOf(
        "GET",
        key,
    ),
).unwrap()

suspend fun ReThis.getDel(key: String): String? = execute(
    listOf(
        "GETDEL",
        key,
    ),
).unwrap()

suspend fun ReThis.getEx(
    key: String,
    option: GetExOption,
): String? = execute(
    listOf(
        "GETEX",
        key,
        option,
    ),
).unwrap()

suspend fun ReThis.getRange(key: String, range: LongRange): String? = execute(
    listOf(
        "GETRANGE",
        key,
        range.first,
        range.last,
    ),
).unwrap()

suspend fun ReThis.getRange(key: String, from: Long, to: Long): String? = execute(
    listOf(
        "GETRANGE",
        key,
        from,
        to,
    ),
).unwrap()

suspend fun ReThis.incr(key: String): Long = execute(
    listOf(
        "INCR",
        key,
    ),
).unwrap() ?: 0

suspend fun ReThis.incrBy(key: String, increment: Long): Long = execute(
    listOf(
        "INCRBY",
        key,
        increment,
    ),
).unwrap() ?: 0

suspend fun ReThis.incrByFloat(key: String, increment: Double): Double? = execute(
    listOf(
        "INCRBYFLOAT",
        key,
        increment,
    ),
).unwrap<String>()?.toDoubleOrNull()

suspend fun ReThis.lcs(key1: String, key2: String): String = execute(
    listOf(
        "LCS",
        key1,
        key2,
    ),
).unwrap() ?: ""

suspend fun ReThis.lcs(
    key1: String,
    key2: String,
    mode: LcsMode.LEN = LcsMode.LEN,
): Long = execute(
    listOfNotNull(
        "LCS",
        key1,
        key2,
        mode,
    ),
).unwrap() ?: 0

suspend fun ReThis.lcs(
    key1: String,
    key2: String,
    mode: LcsMode.IDX,
    len: MinMatchLen? = null,
    withMatchLen: Boolean = false,
): LcsResult {
    val response = execute(
        listOfNotNull(
            "LCS",
            key1,
            key2,
            mode,
            len,
            if (withMatchLen) WITHMATCHLEN else null,
        ),
    )

    val (matches, totalLength) = when (val data = response) {
        is RArray -> Pair(data.value[1].cast<RArray>().value, data.value[3].unwrap<Long>() ?: 0L)

        is RMap -> {
            val mapResponse = data.value.entries.associate { (key, value) ->
                key.unwrap<String>()!! to value
            }

            Pair(
                mapResponse["matches"]?.safeCast<RArray>()?.value ?: exception {
                    "Missing 'matches' field"
                },
                mapResponse["len"]?.unwrap<Long>() ?: 0L,
            )
        }

        else -> exception { "Wrong response type" }
    }

    return LcsResult(processMatches(matches), totalLength)
}

private fun processMatches(matchesArr: List<Any>): List<List<LcsResult.LcsMatch>> =
    matchesArr.map { matchGroup ->
        val length = matchGroup.safeCast<RArray>()?.value?.getOrNull(2)?.let { match ->
            match.safeCast<Int64>()?.value
        }
        matchGroup.cast<RArray>().value.dropLast(1).map { match ->
            if (match is RArray) {
                val range = match.value
                val start = range[0].unwrap<Long>() ?: 0L
                val end = range[1].unwrap<Long>() ?: 0L
                LcsResult.LcsMatch(start = start, end = end, length = length)
            } else {
                exception { "Unexpected match type" }
            }
        }
    }

suspend fun ReThis.mget(vararg key: String): List<String?> = execute(
    listOf(
        "MGET",
        *key,
    ),
).unwrapList()

suspend fun ReThis.mset(vararg kvPair: Pair<String, String>): String? = execute(
    listOf(
        "MSET",
        *kvPair,
    ),
).unwrap()

suspend fun ReThis.msetNx(vararg kvPair: Pair<String, String>): Boolean = execute(
    listOf(
        "MSETNX",
        *kvPair,
    ),
).unwrap<Long>() == 1L

suspend fun ReThis.set(
    key: String,
    value: String,
    vararg options: SetOption,
): String? = execute(
    listOf(
        "SET",
        key,
        value,
        *options,
    ),
).unwrap()

suspend fun ReThis.setRange(key: String, offset: Long, value: String): Long = execute(
    listOf(
        "SETRANGE",
        key,
        offset,
        value,
    ),
).unwrap() ?: 0

suspend fun ReThis.strlen(key: String): Long = execute(
    listOf(
        "STRLEN",
        key,
    ),
).unwrap() ?: 0
