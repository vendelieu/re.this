package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.types.RArray
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.types.common.toArgument
import eu.vendeli.rethis.types.response.MPopResult
import eu.vendeli.rethis.types.response.MoveDirection
import eu.vendeli.rethis.types.response.PopResult
import eu.vendeli.rethis.types.options.LInsertPlace
import eu.vendeli.rethis.types.options.LPosOption
import eu.vendeli.rethis.utils.cast
import eu.vendeli.rethis.utils.writeArgument
import eu.vendeli.rethis.utils.execute
import eu.vendeli.rethis.utils.unwrap
import eu.vendeli.rethis.utils.unwrapList

suspend fun ReThis.blMove(
    source: String,
    destination: String,
    moveFrom: MoveDirection,
    moveTo: MoveDirection,
    timeout: Long,
): String? = execute<String>(
    listOf(
        "BLMOVE".toArgument(),
        source.toArgument(),
        destination.toArgument(),
        moveFrom.toArgument(),
        moveTo.toArgument(),
        timeout.toArgument(),
    ),
)

suspend fun ReThis.blmPop(
    timeout: Long,
    vararg key: String,
    direction: MoveDirection,
    count: Int? = null,
): List<MPopResult> = execute(
    mutableListOf(
        "BLMPOP".toArgument(),
        timeout.toArgument(),
        key.size.toArgument(),
        *key.toArgument(),
        direction.name.uppercase().toArgument(),
    ).apply {
        count?.let { writeArgument("COUNT" to count) }
    },
).unwrapList<RType>().chunked(2) { item ->
    MPopResult(name = item.first().unwrap<String>()!!, poppedElements = item.last().unwrapList())
}

suspend fun ReThis.blPop(
    vararg keys: String,
    timeout: Long = 0,
): PopResult? = execute(
    listOf("BLPOP".toArgument(), *keys.toArgument(), timeout.toArgument()),
).takeIf { it is RArray }?.let {
    val elements = it.cast<RArray>().value
    PopResult(key = elements.first().unwrap()!!, popped = elements.last().unwrap()!!)
}

suspend fun ReThis.brPop(
    timeout: Long,
    vararg keys: String,
): PopResult? = execute(
    listOf("BRPOP".toArgument(), *keys.toArgument(), timeout.toArgument()),
).takeIf { it is RArray }?.let {
    val elements = it.cast<RArray>().value
    PopResult(key = elements.first().unwrap()!!, popped = elements.last().unwrap()!!)
}

suspend fun ReThis.lIndex(
    key: String,
    index: Long,
): String? = execute<String>(
    listOf("LINDEX".toArgument(), key.toArgument(), index.toArgument()),
)

suspend fun ReThis.lInsert(
    key: String,
    place: LInsertPlace,
    pivot: String,
    element: String,
): Long? = execute<Long>(
    listOf(
        "LINSERT".toArgument(),
        key.toArgument(),
        place.toArgument(),
        pivot.toArgument(),
        element.toArgument(),
    ),
)

suspend fun ReThis.lLen(
    key: String,
): Long = execute<Long>(
    listOf("LLEN".toArgument(), key.toArgument()),
) ?: 0

suspend fun ReThis.lMove(
    source: String,
    destination: String,
    moveFrom: MoveDirection,
    moveTo: MoveDirection,
): String? = execute<String>(
    listOf(
        "LMOVE".toArgument(),
        source.toArgument(),
        destination.toArgument(),
        moveFrom.toArgument(),
        moveTo.toArgument(),
    ),
)

suspend fun ReThis.lmPop(
    direction: MoveDirection,
    vararg key: String,
    count: Int? = null,
): List<MPopResult> = execute(
    mutableListOf(
        "LMPOP".toArgument(),
        key.size.toArgument(),
        *key.toArgument(),
        direction.name.uppercase().toArgument(),
    ).apply {
        count?.let { writeArgument("COUNT" to count) }
    },
).unwrapList<RType>().chunked(2) { item ->
    MPopResult(name = item.first().unwrap<String>()!!, poppedElements = item.last().unwrapList())
}

suspend fun ReThis.lPop(
    key: String,
): String? = execute<String>(
    listOf("LPOP".toArgument(), key.toArgument()),
)

suspend fun ReThis.lPop(
    key: String,
    count: Long,
): List<String> = execute(
    listOf("LPOP".toArgument(), key.toArgument(), count.toArgument()),
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.lPos(key: String, element: String, vararg option: LPosOption.CommonOption): Long? =
    execute<Long>(mutableListOf("LPOS".toArgument(), key.toArgument(), element.toArgument()).writeArgument(option))

suspend fun ReThis.lPos(
    key: String,
    element: String,
    count: LPosOption.Count,
    vararg option: LPosOption.CommonOption,
): List<Long> = execute(
    mutableListOf("LPOS".toArgument(), key.toArgument(), element.toArgument()).apply {
        writeArgument(count)
        writeArgument(option)
    },
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.lPush(key: String, vararg elements: String): Long? =
    execute<Long>(listOf("LPUSH".toArgument(), key.toArgument(), *elements.toArgument()))

suspend fun ReThis.lPushX(key: String, vararg elements: String): Long? =
    execute<Long>(listOf("LPUSHX".toArgument(), key.toArgument(), *elements.toArgument()))

suspend fun ReThis.lRem(key: String, count: Long, element: String): Long? =
    execute<Long>(listOf("LREM".toArgument(), key.toArgument(), count.toArgument(), element.toArgument()))

suspend fun ReThis.lRange(key: String, start: Long, stop: Long): List<String> = execute(
    listOf("LRANGE".toArgument(), key.toArgument(), start.toArgument(), stop.toArgument()),
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.lSet(key: String, index: Long, element: String): String? =
    execute<String>(listOf("LSET".toArgument(), key.toArgument(), index.toArgument(), element.toArgument()))

suspend fun ReThis.lTrim(key: String, range: IntRange): String? =
    execute<String>(listOf("LTRIM".toArgument(), key.toArgument(), range.first.toArgument(), range.last.toArgument()))

suspend fun ReThis.rPop(key: String): String? =
    execute<String>(listOf("RPOP".toArgument(), key.toArgument()))

suspend fun ReThis.rPop(key: String, count: Long): List<String> =
    execute(listOf("RPOP".toArgument(), key.toArgument(), count.toArgument()), isCollectionResponse = true)
        ?: emptyList()

suspend fun ReThis.rPush(key: String, vararg elements: String): Long? =
    execute<Long>(listOf("RPUSH".toArgument(), key.toArgument(), *elements.toArgument()))

suspend fun ReThis.rPushX(key: String, vararg elements: String): Long? =
    execute<Long>(listOf("RPUSHX".toArgument(), key.toArgument(), *elements.toArgument()))
