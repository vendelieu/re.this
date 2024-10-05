package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.types.common.MPopResult
import eu.vendeli.rethis.types.common.MoveDirection
import eu.vendeli.rethis.types.common.PopResult
import eu.vendeli.rethis.types.core.*
import eu.vendeli.rethis.types.core.unwrap
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
): String? = execute(
    listOf(
        "BLMOVE".toArg(),
        source.toArg(),
        destination.toArg(),
        moveFrom.toArg(),
        moveTo.toArg(),
        timeout.toArg(),
    ),
).unwrap()

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
): String? = execute(
    listOf("LINDEX".toArg(), key.toArg(), index.toArg()),
).unwrap()

suspend fun ReThis.lInsert(
    key: String,
    place: LInsertPlace,
    pivot: String,
    element: String,
): Long? = execute(
    listOf(
        "LINSERT".toArg(),
        key.toArg(),
        place.toArg(),
        pivot.toArg(),
        element.toArg(),
    ),
).unwrap()

suspend fun ReThis.lLen(
    key: String,
): Long = execute(
    listOf("LLEN".toArg(), key.toArg()),
).unwrap() ?: 0

suspend fun ReThis.lMove(
    source: String,
    destination: String,
    moveFrom: MoveDirection,
    moveTo: MoveDirection,
): String? = execute(
    listOf(
        "LMOVE".toArg(),
        source.toArg(),
        destination.toArg(),
        moveFrom.toArg(),
        moveTo.toArg(),
    ),
).unwrap()

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
): String? = execute(
    listOf("LPOP".toArg(), key.toArg()),
).unwrap()

suspend fun ReThis.lPop(
    key: String,
    count: Long,
): List<String> = execute(
    listOf("LPOP".toArg(), key.toArg(), count.toArg()),
).unwrapList<String>()

suspend fun ReThis.lPos(key: String, element: String, vararg option: LPosOption.CommonOption): Long? =
    execute(mutableListOf("LPOS".toArg(), key.toArg(), element.toArg()).writeArg(option)).unwrap()

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
).unwrapList()

suspend fun ReThis.lPush(key: String, vararg elements: String): Long? =
    execute(listOf("LPUSH".toArg(), key.toArg(), *elements.toArg())).unwrap()

suspend fun ReThis.lPushX(key: String, vararg elements: String): Long? =
    execute(listOf("LPUSHX".toArg(), key.toArg(), *elements.toArg())).unwrap()

suspend fun ReThis.lRem(key: String, count: Long, element: String): Long? =
    execute(listOf("LREM".toArg(), key.toArg(), count.toArg(), element.toArg())).unwrap()

suspend fun ReThis.lSet(key: String, index: Long, element: String): String? =
    execute(listOf("LSET".toArg(), key.toArg(), index.toArg(), element.toArg())).unwrap<String>()

suspend fun ReThis.lTrim(key: String, range: IntRange): String? =
    execute(listOf("LTRIM".toArg(), key.toArg(), range.first.toArg(), range.last.toArg())).unwrap<String>()

suspend fun ReThis.rPop(key: String): String? =
    execute(listOf("RPOP".toArg(), key.toArg())).unwrap()

suspend fun ReThis.rPop(key: String, count: Long): List<String> =
    execute(listOf("RPOP".toArg(), key.toArg(), count.toArg())).unwrapList()

suspend fun ReThis.rPush(key: String, vararg elements: String): Long? =
    execute(listOf("RPUSH".toArg(), key.toArg(), *elements.toArg())).unwrap()

suspend fun ReThis.rPushX(key: String, vararg elements: String): Long? =
    execute(listOf("RPUSHX".toArg(), key.toArg(), *elements.toArg())).unwrap()
