package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.types.common.MPopResult
import eu.vendeli.rethis.types.common.MoveDirection
import eu.vendeli.rethis.types.common.PopResult
import eu.vendeli.rethis.types.options.LInsertPlace
import eu.vendeli.rethis.types.options.LPosOption
import eu.vendeli.rethis.types.core.RArray
import eu.vendeli.rethis.types.core.RType
import eu.vendeli.rethis.types.core.unwrap
import eu.vendeli.rethis.types.core.unwrapList
import eu.vendeli.rethis.utils.cast

suspend fun ReThis.blMove(
    source: String,
    destination: String,
    moveFrom: MoveDirection,
    moveTo: MoveDirection,
    timeout: Long,
): String? = execute(
    listOf(
        "BLMOVE",
        source,
        destination,
        moveFrom,
        moveTo,
        timeout,
    ),
).unwrap()

suspend fun ReThis.blmPop(
    timeout: Long,
    vararg key: String,
    direction: MoveDirection,
    count: Int? = null,
): List<MPopResult> = execute(
    listOfNotNull(
        "BLMPOP",
        timeout,
        key.size,
        *key,
        direction.name.uppercase(),
        if (count != null) "COUNT" to count else null,
    ),
).unwrapList<RType>().chunked(2) { item ->
    MPopResult(name = item.first().unwrap<String>()!!, poppedElements = item.last().unwrapList())
}

suspend fun ReThis.blPop(
    vararg keys: String,
    timeout: Long = 0,
): PopResult? = execute(
    listOf("BLPOP", *keys, timeout),
).takeIf { it is RArray }?.let {
    val elements = it.cast<RArray>().value
    PopResult(key = elements.first().unwrap()!!, popped = elements.last().unwrap()!!)
}

suspend fun ReThis.brPop(
    timeout: Long,
    vararg keys: String,
): PopResult? = execute(
    listOf("BRPOP", *keys, timeout),
).takeIf { it is RArray }?.let {
    val elements = it.cast<RArray>().value
    PopResult(key = elements.first().unwrap()!!, popped = elements.last().unwrap()!!)
}

suspend fun ReThis.lIndex(
    key: String,
    index: Long,
): String? = execute(
    listOf("LINDEX", key, index),
).unwrap()

suspend fun ReThis.lInsert(
    key: String,
    place: LInsertPlace,
    pivot: String,
    element: String,
): Long? = execute(
    listOf(
        "LINSERT",
        key,
        place,
        pivot,
        element,
    ),
).unwrap()

suspend fun ReThis.lLen(
    key: String,
): Long = execute(
    listOf("LLEN", key),
).unwrap() ?: 0

suspend fun ReThis.lMove(
    source: String,
    destination: String,
    moveFrom: MoveDirection,
    moveTo: MoveDirection,
): String? = execute(
    listOf(
        "LMOVE",
        source,
        destination,
        moveFrom,
        moveTo,
    ),
).unwrap()

suspend fun ReThis.lmPop(
    direction: MoveDirection,
    vararg key: String,
    count: Int? = null,
): List<MPopResult> = execute(
    listOfNotNull(
        "LMPOP",
        key.size,
        *key,
        direction.name.uppercase(),
        if (count != null) "COUNT" to count else null,
    ),
).unwrapList<RType>().chunked(2) { item ->
    MPopResult(name = item.first().unwrap<String>()!!, poppedElements = item.last().unwrapList())
}

suspend fun ReThis.lPop(
    key: String,
): String? = execute(
    listOf("LPOP", key),
).unwrap()

suspend fun ReThis.lPop(
    key: String,
    count: Long,
): List<String> = execute(
    listOf("LPOP", key, count),
).unwrapList<String>()

suspend fun ReThis.lPos(key: String, element: String, vararg option: LPosOption.CommonOption): Long? =
    execute(listOf("LPOS", key, element, *option)).unwrap()

suspend fun ReThis.lPos(
    key: String,
    element: String,
    count: LPosOption.Count,
    vararg option: LPosOption.CommonOption,
): List<Long> = execute(listOf("LPOS", key, element, count, *option)).unwrapList()

suspend fun ReThis.lPush(key: String, vararg elements: String): Long? =
    execute(listOf("LPUSH", key, *elements)).unwrap()

suspend fun ReThis.lPushX(key: String, vararg elements: String): Long? =
    execute(listOf("LPUSHX", key, *elements)).unwrap()

suspend fun ReThis.lRem(key: String, count: Long, element: String): Long? =
    execute(listOf("LREM", key, count, element)).unwrap()

suspend fun ReThis.lSet(key: String, index: Long, element: String): String? =
    execute(listOf("LSET", key, index, element)).unwrap<String>()

suspend fun ReThis.lTrim(key: String, start: Long, stop: Long): String? =
    execute(listOf("LTRIM", key, start, stop)).unwrap<String>()

suspend fun ReThis.rPop(key: String): String? =
    execute(listOf("RPOP", key)).unwrap()

suspend fun ReThis.rPop(key: String, count: Long): List<String> =
    execute(listOf("RPOP", key, count)).unwrapList()

suspend fun ReThis.rPush(key: String, vararg elements: String): Long? =
    execute(listOf("RPUSH", key, *elements)).unwrap()

suspend fun ReThis.rPushX(key: String, vararg elements: Any?): Long? =
    execute(listOf("RPUSHX", key, *elements)).unwrap()
