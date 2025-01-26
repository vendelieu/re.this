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

suspend fun ReThis.append(key: String, value: String): Long = execute<Long>(
    listOf(
        "APPEND".toArg(),
        key.toArg(),
        value.toArg(),
    ),
) ?: 0

suspend fun ReThis.decr(key: String): Long = execute<Long>(
    listOf(
        "DECR".toArg(),
        key.toArg(),
    ),
) ?: 0

suspend fun ReThis.decrBy(key: String, decrement: Long): Long = execute<Long>(
    listOf(
        "DECRBY".toArg(),
        key.toArg(),
        decrement.toArg(),
    ),
) ?: 0

suspend fun ReThis.get(key: String): String? = execute<String>(
    listOf(
        "GET".toArg(),
        key.toArg(),
    ),
)

suspend fun ReThis.getDel(key: String): String? = execute<String>(
    listOf(
        "GETDEL".toArg(),
        key.toArg(),
    ),
)

suspend fun ReThis.getEx(
    key: String,
    option: GetExOption,
): String? = execute<String>(
    mutableListOf(
        "GETEX".toArg(),
        key.toArg(),
    ).writeArg(option),
)

suspend fun ReThis.getRange(key: String, range: LongRange): String? = execute<String>(
    listOf(
        "GETRANGE".toArg(),
        key.toArg(),
        range.first.toArg(),
        range.last.toArg(),
    ),
)

suspend fun ReThis.getRange(key: String, from: Long, to: Long): String? = execute<String>(
    listOf(
        "GETRANGE".toArg(),
        key.toArg(),
        from.toArg(),
        to.toArg(),
    ),
)

suspend fun ReThis.incr(key: String): Long = execute<Long>(
    listOf(
        "INCR".toArg(),
        key.toArg(),
    ),
) ?: 0

suspend fun ReThis.incrBy(key: String, increment: Long): Long = execute<Long>(
    listOf(
        "INCRBY".toArg(),
        key.toArg(),
        increment.toArg(),
    ),
) ?: 0

suspend fun ReThis.incrByFloat(key: String, increment: Double): Double? = execute<String>(
    listOf(
        "INCRBYFLOAT".toArg(),
        key.toArg(),
        increment.toArg(),
    ),
)?.toDouble()

suspend fun ReThis.lcs(key1: String, key2: String): String? = execute<String>(
    listOf(
        "LCS".toArg(),
        key1.toArg(),
        key2.toArg(),
    ),
)

suspend fun ReThis.lcs(
    key1: String,
    key2: String,
    mode: LcsMode.LEN = LcsMode.LEN,
): Long = execute<Long>(
    mutableListOf(
        "LCS".toArg(),
        key1.toArg(),
        key2.toArg(),
    ).writeArg(mode),
) ?: 0

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
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.mset(vararg kvPair: Pair<String, String>): String? = execute<String>(
    mutableListOf(
        "MSET".toArg(),
    ).apply { kvPair.forEach { writeArg(it) } },
)

suspend fun ReThis.msetNx(vararg kvPair: Pair<String, String>): Boolean = execute<Long>(
    mutableListOf(
        "MSETNX".toArg(),
    ).apply { kvPair.forEach { writeArg(it) } },
) == 1L

suspend fun ReThis.set(
    key: String,
    value: String,
    vararg options: SetOption,
): String? = execute<String>(
    mutableListOf(
        "SET".toArg(),
        key.toArg(),
        value.toArg(),
    ).apply { options.forEach { writeArg(it) } },
)

suspend fun ReThis.setRange(key: String, offset: Long, value: String): Long = execute<Long>(
    listOf(
        "SETRANGE".toArg(),
        key.toArg(),
        offset.toArg(),
        value.toArg(),
    ),
) ?: 0

suspend fun ReThis.strlen(key: String): Long = execute<Long>(
    listOf(
        "STRLEN".toArg(),
        key.toArg(),
    ),
) ?: 0
