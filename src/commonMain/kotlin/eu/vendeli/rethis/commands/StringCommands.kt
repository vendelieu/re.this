package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.exception
import eu.vendeli.rethis.types.common.LcsResult
import eu.vendeli.rethis.types.core.*
import eu.vendeli.rethis.types.options.GetExOption
import eu.vendeli.rethis.types.options.LcsMode
import eu.vendeli.rethis.types.options.MinMatchLen
import eu.vendeli.rethis.types.options.SetOption
import eu.vendeli.rethis.utils.cast
import eu.vendeli.rethis.utils.safeCast
import eu.vendeli.rethis.utils.writeArg

suspend fun ReThis.append(key: String, value: String): Long = execute(
    listOf(
        "APPEND".toArg(),
        key.toArg(),
        value.toArg(),
    ),
).unwrap() ?: 0

suspend fun ReThis.decr(key: String): Long = execute(
    listOf(
        "DECR".toArg(),
        key.toArg(),
    ),
).unwrap() ?: 0

suspend fun ReThis.decrBy(key: String, decrement: Long): Long = execute(
    listOf(
        "DECRBY".toArg(),
        key.toArg(),
        decrement.toArg(),
    ),
).unwrap() ?: 0

suspend fun ReThis.get(key: String): String? = execute(
    listOf(
        "GET".toArg(),
        key.toArg(),
    ),
).unwrap()

suspend fun ReThis.getDel(key: String): String? = execute(
    listOf(
        "GETDEL".toArg(),
        key.toArg(),
    ),
).unwrap()

suspend fun ReThis.getEx(
    key: String,
    option: GetExOption,
): String? = execute(
    mutableListOf(
        "GETEX".toArg(),
        key.toArg(),
    ).writeArg(option),
).unwrap()

suspend fun ReThis.getRange(key: String, range: LongRange): String? = execute(
    listOf(
        "GETRANGE".toArg(),
        key.toArg(),
        range.first.toArg(),
        range.last.toArg(),
    ),
).unwrap()

suspend fun ReThis.getRange(key: String, from: Long, to: Long): String? = execute(
    listOf(
        "GETRANGE".toArg(),
        key.toArg(),
        from.toArg(),
        to.toArg(),
    ),
).unwrap()

suspend fun ReThis.incr(key: String): Long = execute(
    listOf(
        "INCR".toArg(),
        key.toArg(),
    ),
).unwrap() ?: 0

suspend fun ReThis.incrBy(key: String, increment: Long): Long = execute(
    listOf(
        "INCRBY".toArg(),
        key.toArg(),
        increment.toArg(),
    ),
).unwrap() ?: 0

suspend fun ReThis.incrByFloat(key: String, increment: Double): Double? = execute(
    listOf(
        "INCRBYFLOAT".toArg(),
        key.toArg(),
        increment.toArg(),
    ),
).unwrap<String>()?.toDoubleOrNull()

suspend fun ReThis.lcs(key1: String, key2: String): String = execute(
    listOf(
        "LCS".toArg(),
        key1.toArg(),
        key2.toArg(),
    ),
).unwrap() ?: ""

suspend fun ReThis.lcs(
    key1: String,
    key2: String,
    mode: LcsMode.LEN = LcsMode.LEN,
): Long = execute(
    mutableListOf(
        "LCS".toArg(),
        key1.toArg(),
        key2.toArg(),
    ).writeArg(mode),
).unwrap() ?: 0

suspend fun ReThis.lcs(
    key1: String,
    key2: String,
    mode: LcsMode.IDX,
    len: MinMatchLen? = null,
    withMatchLen: Boolean = false,
): LcsResult {
    val response = execute(
        mutableListOf(
            "LCS".toArg(),
            key1.toArg(),
            key2.toArg(),
        ).apply {
            writeArg(mode)
            writeArg(len)
            if (withMatchLen) writeArg("WITHMATCHLEN")
        },
    )

    val (matches, totalLength) = when (response) {
        is RArray -> Pair(response.value[1].cast<RArray>().value, response.value[3].unwrap<Long>() ?: 0L)

        is RMap -> {
            val mapResponse = response.value.entries.associate { (key, value) ->
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
        "MGET".toArg(),
        *key.toArg(),
    ),
).unwrapList()

suspend fun ReThis.mset(vararg kvPair: Pair<String, String>): String? = execute(
    mutableListOf(
        "MSET".toArg(),
    ).apply { kvPair.forEach { writeArg(it) } },
).unwrap()

suspend fun ReThis.msetNx(vararg kvPair: Pair<String, String>): Boolean = execute(
    mutableListOf(
        "MSETNX".toArg(),
    ).apply { kvPair.forEach { writeArg(it) } },
).unwrap<Long>() == 1L

suspend fun ReThis.set(
    key: String,
    value: String,
    vararg options: SetOption,
): String? = execute(
    mutableListOf(
        "SET".toArg(),
        key.toArg(),
        value.toArg(),
    ).apply { options.forEach { writeArg(it) } },
).unwrap()

suspend fun ReThis.setRange(key: String, offset: Long, value: String): Long = execute(
    listOf(
        "SETRANGE".toArg(),
        key.toArg(),
        offset.toArg(),
        value.toArg(),
    ),
).unwrap() ?: 0

suspend fun ReThis.strlen(key: String): Long = execute(
    listOf(
        "STRLEN".toArg(),
        key.toArg(),
    ),
).unwrap() ?: 0
