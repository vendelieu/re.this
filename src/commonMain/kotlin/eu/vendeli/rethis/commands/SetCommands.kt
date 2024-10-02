package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.exception
import eu.vendeli.rethis.types.common.ScanResult
import eu.vendeli.rethis.types.core.RArray
import eu.vendeli.rethis.types.core.unwrap
import eu.vendeli.rethis.types.core.unwrapList
import eu.vendeli.rethis.types.core.unwrapSet
import eu.vendeli.rethis.types.options.SScanOption
import eu.vendeli.rethis.utils.safeCast

suspend fun ReThis.sAdd(key: String, vararg members: String): Long = execute(
    listOf(
        "SADD",
        key,
        *members,
    ),
).unwrap() ?: 0

suspend fun ReThis.sCard(key: String): Long = execute(
    listOf(
        "SCARD",
        key,
    ),
).unwrap() ?: 0

suspend fun ReThis.sDiff(vararg keys: String): Set<String> = execute(
    listOf(
        "SDIFF",
        *keys,
    ),
).unwrapSet<String>()

suspend fun ReThis.sDiffStore(destination: String, vararg keys: String): Long = execute(
    listOf(
        "SDIFFSTORE",
        destination,
        *keys,
    ),
).unwrap() ?: 0

suspend fun ReThis.sInter(vararg keys: String): Set<String> = execute(
    listOf(
        "SINTER",
        *keys,
    ),
).unwrapSet<String>()

suspend fun ReThis.sInterStore(destination: String, vararg keys: String): Long = execute(
    listOf(
        "SINTERSTORE",
        destination,
        *keys,
    ),
).unwrap() ?: 0

suspend fun ReThis.sIsMember(key: String, member: String): Boolean = execute(
    listOf(
        "SISMEMBER",
        key,
        member,
    ),
).unwrap<Long>() == 1L

suspend fun ReThis.sMembers(key: String): Set<String> = execute(
    listOf(
        "SMEMBERS",
        key,
    ),
).unwrapSet<String>()

suspend fun ReThis.sMove(source: String, destination: String, member: String): Boolean = execute(
    listOf(
        "SMOVE",
        source,
        destination,
        member,
    ),
).unwrap<Long>() == 1L

suspend fun ReThis.sPop(
    key: String,
): String? = execute(
    listOf("SPOP", key),
).unwrap()

suspend fun ReThis.sPop(
    key: String,
    count: Long,
): Set<String> = execute(
    listOf("SPOP", key, count),
).unwrapSet<String>()

suspend fun ReThis.sRandMember(
    key: String,
): String? = execute(
    listOf("SRANDMEMBER", key),
).unwrap()

suspend fun ReThis.sRandMember(
    key: String,
    count: Long,
): List<String> = execute(
    listOf("SRANDMEMBER", key, count),
).unwrapList<String>()

suspend fun ReThis.sRem(key: String, vararg members: String): Long = execute(
    listOf(
        "SREM",
        key,
        *members,
    ),
).unwrap() ?: 0

suspend fun ReThis.sScan(
    key: String,
    cursor: Long,
    vararg option: SScanOption,
): ScanResult<String> {
    val response = execute(listOf("SSCAN", key, cursor, *option))

    val arrResponse = response.safeCast<RArray>()?.value ?: exception { "Wrong response type" }
    val newCursor = arrResponse[0].unwrap<String>() ?: exception { "Missing cursor in response" }

    val membersArray = arrResponse[1].safeCast<RArray>()?.value ?: exception { "Missing members in response" }
    val members = membersArray.map { it.unwrap<String>() ?: exception { "Invalid member format" } }

    return ScanResult(cursor = newCursor, keys = members)
}

suspend fun ReThis.sUnion(vararg keys: String): Set<String> = execute(
    listOf(
        "SUNION",
        *keys,
    ),
).unwrapSet<String>()

suspend fun ReThis.sUnionStore(destination: String, vararg keys: String): Long = execute(
    listOf(
        "SUNIONSTORE",
        destination,
        *keys,
    ),
).unwrap() ?: 0

suspend fun ReThis.sInterCard(vararg keys: String, limit: Long? = null): Long = execute(
    listOfNotNull(
        "SINTERCARD",
        keys.size,
        *keys,
        limit?.let { "LIMIT" to it },
    ),
).unwrap() ?: 0

suspend fun ReThis.sMisMember(key: String, vararg members: String): List<Boolean> = execute(
    listOf(
        "SMISMEMBER",
        key,
        *members,
    ),
).unwrapList<Long>().map { it == 1L }
