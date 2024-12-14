package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.types.common.MPopResult
import eu.vendeli.rethis.types.common.MoveDirection
import eu.vendeli.rethis.types.common.PopResult
import eu.vendeli.rethis.types.core.*
import eu.vendeli.rethis.types.options.LInsertPlace
import eu.vendeli.rethis.types.options.LPosOption
import eu.vendeli.rethis.utils.cast
import eu.vendeli.rethis.utils.writeArg

suspend fun ReThis.blMove(
    source: String,
    destination: String,
    moveFrom: MoveDirection,
    moveTo: MoveDirection,
    timeout: Long,
): String? = execute<String>(
    listOf(
        "BLMOVE".toArg(),
        source.toArg(),
        destination.toArg(),
        moveFrom.toArg(),
        moveTo.toArg(),
        timeout.toArg(),
    ),
)

suspend fun ReThis.blmPop(
    timeout: Long,
    vararg key: String,
    direction: MoveDirection,
    count: Int? = null,
): List<MPopResult> = execute(
    mutableListOf(
        "BLMPOP".toArg(),
        timeout.toArg(),
        key.size.toArg(),
        *key.toArg(),
        direction.name.uppercase().toArg(),
    ).apply {
        count?.let { writeArg("COUNT" to count) }
    },
).unwrapList<RType>().chunked(2) { item ->
    MPopResult(name = item.first().unwrap<String>()!!, poppedElements = item.last().unwrapList())
}

suspend fun ReThis.blPop(
    vararg keys: String,
    timeout: Long = 0,
): PopResult? = execute(
    listOf("BLPOP".toArg(), *keys.toArg(), timeout.toArg()),
).takeIf { it is RArray }?.let {
    val elements = it.cast<RArray>().value
    PopResult(key = elements.first().unwrap()!!, popped = elements.last().unwrap()!!)
}

suspend fun ReThis.brPop(
    timeout: Long,
    vararg keys: String,
): PopResult? = execute(
    listOf("BRPOP".toArg(), *keys.toArg(), timeout.toArg()),
).takeIf { it is RArray }?.let {
    val elements = it.cast<RArray>().value
    PopResult(key = elements.first().unwrap()!!, popped = elements.last().unwrap()!!)
}

suspend fun ReThis.lIndex(
    key: String,
    index: Long,
): String? = execute<String>(
    listOf("LINDEX".toArg(), key.toArg(), index.toArg()),
)

suspend fun ReThis.lInsert(
    key: String,
    place: LInsertPlace,
    pivot: String,
    element: String,
): Long? = execute<Long>(
    listOf(
        "LINSERT".toArg(),
        key.toArg(),
        place.toArg(),
        pivot.toArg(),
        element.toArg(),
    ),
)

suspend fun ReThis.lLen(
    key: String,
): Long = execute<Long>(
    listOf("LLEN".toArg(), key.toArg()),
) ?: 0

suspend fun ReThis.lMove(
    source: String,
    destination: String,
    moveFrom: MoveDirection,
    moveTo: MoveDirection,
): String? = execute<String>(
    listOf(
        "LMOVE".toArg(),
        source.toArg(),
        destination.toArg(),
        moveFrom.toArg(),
        moveTo.toArg(),
    ),
)

suspend fun ReThis.lmPop(
    direction: MoveDirection,
    vararg key: String,
    count: Int? = null,
): List<MPopResult> = execute(
    mutableListOf(
        "LMPOP".toArg(),
        key.size.toArg(),
        *key.toArg(),
        direction.name.uppercase().toArg(),
    ).apply {
        count?.let { writeArg("COUNT" to count) }
    },
).unwrapList<RType>().chunked(2) { item ->
    MPopResult(name = item.first().unwrap<String>()!!, poppedElements = item.last().unwrapList())
}

suspend fun ReThis.lPop(
    key: String,
): String? = execute<String>(
    listOf("LPOP".toArg(), key.toArg()),
)

suspend fun ReThis.lPop(
    key: String,
    count: Long,
): List<String> = execute(
    listOf("LPOP".toArg(), key.toArg(), count.toArg()),
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.lPos(key: String, element: String, vararg option: LPosOption.CommonOption): Long? =
    execute<Long>(mutableListOf("LPOS".toArg(), key.toArg(), element.toArg()).writeArg(option))

suspend fun ReThis.lPos(
    key: String,
    element: String,
    count: LPosOption.Count,
    vararg option: LPosOption.CommonOption,
): List<Long> = execute(
    mutableListOf("LPOS".toArg(), key.toArg(), element.toArg()).apply {
        writeArg(count)
        writeArg(option)
    },
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.lPush(key: String, vararg elements: String): Long? =
    execute<Long>(listOf("LPUSH".toArg(), key.toArg(), *elements.toArg()))

suspend fun ReThis.lPushX(key: String, vararg elements: String): Long? =
    execute<Long>(listOf("LPUSHX".toArg(), key.toArg(), *elements.toArg()))

suspend fun ReThis.lRem(key: String, count: Long, element: String): Long? =
    execute<Long>(listOf("LREM".toArg(), key.toArg(), count.toArg(), element.toArg()))

suspend fun ReThis.lRange(key: String, start: Long, stop: Long): List<String> = execute(
    listOf("LRANGE".toArg(), key.toArg(), start.toArg(), stop.toArg()),
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.lSet(key: String, index: Long, element: String): String? =
    execute<String>(listOf("LSET".toArg(), key.toArg(), index.toArg(), element.toArg()))

suspend fun ReThis.lTrim(key: String, range: IntRange): String? =
    execute<String>(listOf("LTRIM".toArg(), key.toArg(), range.first.toArg(), range.last.toArg()))

suspend fun ReThis.rPop(key: String): String? =
    execute<String>(listOf("RPOP".toArg(), key.toArg()))

suspend fun ReThis.rPop(key: String, count: Long): List<String> =
    execute(listOf("RPOP".toArg(), key.toArg(), count.toArg()), isCollectionResponse = true) ?: emptyList()

suspend fun ReThis.rPush(key: String, vararg elements: String): Long? =
    execute<Long>(listOf("RPUSH".toArg(), key.toArg(), *elements.toArg()))

suspend fun ReThis.rPushX(key: String, vararg elements: String): Long? =
    execute<Long>(listOf("RPUSHX".toArg(), key.toArg(), *elements.toArg()))
