package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.processingException
import eu.vendeli.rethis.types.common.LcsResult
import eu.vendeli.rethis.types.core.*
import eu.vendeli.rethis.types.options.GetExOption
import eu.vendeli.rethis.types.options.LcsMode
import eu.vendeli.rethis.types.options.MinMatchLen
import eu.vendeli.rethis.types.options.SetOption
import eu.vendeli.rethis.utils.*

suspend fun ReThis.append(key: String, value: String): Long = execute<Long>(
    listOf(
        "APPEND".toArgument(),
        key.toArgument(),
        value.toArgument(),
    ),
) ?: 0

suspend fun ReThis.decr(key: String): Long = execute<Long>(
    listOf(
        "DECR".toArgument(),
        key.toArgument(),
    ),
) ?: 0

suspend fun ReThis.decrBy(key: String, decrement: Long): Long = execute<Long>(
    listOf(
        "DECRBY".toArgument(),
        key.toArgument(),
        decrement.toArgument(),
    ),
) ?: 0

suspend fun ReThis.get(key: String): String? = execute<String>(
    listOf(
        "GET".toArgument(),
        key.toArgument(),
    ),
)

suspend fun ReThis.getDel(key: String): String? = execute<String>(
    listOf(
        "GETDEL".toArgument(),
        key.toArgument(),
    ),
)

suspend fun ReThis.getEx(
    key: String,
    option: GetExOption,
): String? = execute<String>(
    mutableListOf(
        "GETEX".toArgument(),
        key.toArgument(),
    ).writeArgument(option),
)

suspend fun ReThis.getRange(key: String, range: LongRange): String? = execute<String>(
    listOf(
        "GETRANGE".toArgument(),
        key.toArgument(),
        range.first.toArgument(),
        range.last.toArgument(),
    ),
)

suspend fun ReThis.getRange(key: String, from: Long, to: Long): String? = execute<String>(
    listOf(
        "GETRANGE".toArgument(),
        key.toArgument(),
        from.toArgument(),
        to.toArgument(),
    ),
)

suspend fun ReThis.incr(key: String): Long = execute<Long>(
    listOf(
        "INCR".toArgument(),
        key.toArgument(),
    ),
) ?: 0

suspend fun ReThis.incrBy(key: String, increment: Long): Long = execute<Long>(
    listOf(
        "INCRBY".toArgument(),
        key.toArgument(),
        increment.toArgument(),
    ),
) ?: 0

suspend fun ReThis.incrByFloat(key: String, increment: Double): Double? = execute<String>(
    listOf(
        "INCRBYFLOAT".toArgument(),
        key.toArgument(),
        increment.toArgument(),
    ),
)?.toDouble()

suspend fun ReThis.lcs(key1: String, key2: String): String? = execute<String>(
    listOf(
        "LCS".toArgument(),
        key1.toArgument(),
        key2.toArgument(),
    ),
)

suspend fun ReThis.lcs(
    key1: String,
    key2: String,
    mode: LcsMode.LEN = LcsMode.LEN,
): Long = execute<Long>(
    mutableListOf(
        "LCS".toArgument(),
        key1.toArgument(),
        key2.toArgument(),
    ).writeArgument(mode),
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
            "LCS".toArgument(),
            key1.toArgument(),
            key2.toArgument(),
        ).apply {
            writeArgument(mode)
            writeArgument(len)
            if (withMatchLen) writeArgument("WITHMATCHLEN")
        },
    )

    val (matches, totalLength) = when (response) {
        is RArray -> Pair(response.value[1].cast<RArray>().value, response.value[3].unwrap<Long>() ?: 0L)

        is RMap -> {
            val mapResponse = response.value.entries.associate { (key, value) ->
                key.unwrap<String>()!! to value
            }

            Pair(
                mapResponse["matches"]?.safeCast<RArray>()?.value ?: processingException {
                    "Missing 'matches' field"
                },
                mapResponse["len"]?.unwrap<Long>() ?: 0L,
            )
        }

        else -> processingException { "Wrong response type" }
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
                processingException { "Unexpected match type" }
            }
        }
    }

suspend fun ReThis.mGet(vararg key: String): List<String?> = execute(
    listOf(
        "MGET".toArgument(),
        *key.toArgument(),
    ),
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.mSet(vararg kvPair: Pair<String, String>): Boolean = execute<String>(
    mutableListOf(
        "MSET".toArgument(),
    ).apply { kvPair.forEach { writeArgument(it) } },
) == "OK"

suspend fun ReThis.mSetNx(vararg kvPair: Pair<String, String>): Boolean = execute<Long>(
    mutableListOf(
        "MSETNX".toArgument(),
    ).apply { kvPair.forEach { writeArgument(it) } },
) == 1L

suspend fun ReThis.set(
    key: String,
    value: String,
    vararg options: SetOption,
): String? = execute<String>(
    mutableListOf(
        "SET".toArgument(),
        key.toArgument(),
        value.toArgument(),
    ).apply { options.forEach { writeArgument(it) } },
)

suspend fun ReThis.setRange(key: String, offset: Long, value: String): Long = execute<Long>(
    listOf(
        "SETRANGE".toArgument(),
        key.toArgument(),
        offset.toArgument(),
        value.toArgument(),
    ),
) ?: 0

suspend fun ReThis.strlen(key: String): Long = execute<Long>(
    listOf(
        "STRLEN".toArgument(),
        key.toArgument(),
    ),
) ?: 0
