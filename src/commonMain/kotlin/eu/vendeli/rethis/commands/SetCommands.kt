package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.exception
import eu.vendeli.rethis.types.common.ScanResult
import eu.vendeli.rethis.types.core.*
import eu.vendeli.rethis.types.core.unwrap
import eu.vendeli.rethis.types.options.SScanOption
import eu.vendeli.rethis.utils.safeCast
import eu.vendeli.rethis.utils.writeArg
import kotlin.Long

suspend fun ReThis.sAdd(key: String, vararg members: String): Long = execute<Long>(
    listOf(
        "SADD".toArg(),
        key.toArg(),
        *members.toArg(),
    ),
) ?: 0

suspend fun ReThis.sCard(key: String): Long = execute<Long>(
    listOf(
        "SCARD".toArg(),
        key.toArg(),
    ),
) ?: 0

suspend fun ReThis.sDiff(vararg keys: String): Set<String> = execute<String>(
    listOf(
        "SDIFF".toArg(),
        *keys.toArg(),
    ),
    isCollectionResponse = true,
)?.toSet() ?: emptySet()

suspend fun ReThis.sDiffStore(destination: String, vararg keys: String): Long = execute<Long>(
    listOf(
        "SDIFFSTORE".toArg(),
        destination.toArg(),
        *keys.toArg(),
    ),
) ?: 0

suspend fun ReThis.sInter(vararg keys: String): Set<String> = execute<String>(
    listOf(
        "SINTER".toArg(),
        *keys.toArg(),
    ),
    isCollectionResponse = true,
)?.toSet() ?: emptySet()

suspend fun ReThis.sInterStore(destination: String, vararg keys: String): Long = execute<Long>(
    listOf(
        "SINTERSTORE".toArg(),
        destination.toArg(),
        *keys.toArg(),
    ),
) ?: 0

suspend fun ReThis.sIsMember(key: String, member: String): Boolean = execute<Long>(
    listOf(
        "SISMEMBER".toArg(),
        key.toArg(),
        member.toArg(),
    ),
) == 1L

suspend fun ReThis.sMembers(key: String): Set<String> = execute<String>(
    listOf(
        "SMEMBERS".toArg(),
        key.toArg(),
    ),
    isCollectionResponse = true,
)?.toSet() ?: emptySet()

suspend fun ReThis.sMove(source: String, destination: String, member: String): Boolean = execute<Long>(
    listOf(
        "SMOVE".toArg(),
        source.toArg(),
        destination.toArg(),
        member.toArg(),
    ),
) == 1L

suspend fun ReThis.sPop(
    key: String,
): String? = execute<String>(
    listOf("SPOP".toArg(), key.toArg()),
)

suspend fun ReThis.sPop(
    key: String,
    count: Long,
): Set<String> = execute<String>(
    listOf("SPOP".toArg(), key.toArg(), count.toArg()),
    isCollectionResponse = true,
)?.toSet() ?: emptySet()

suspend fun ReThis.sRandMember(
    key: String,
): String? = execute<String>(
    listOf("SRANDMEMBER".toArg(), key.toArg()),
)

suspend fun ReThis.sRandMember(
    key: String,
    count: Long,
): List<String> = execute(
    listOf("SRANDMEMBER".toArg(), key.toArg(), count.toArg()),
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.sRem(key: String, vararg members: String): Long = execute<Long>(
    listOf(
        "SREM".toArg(),
        key.toArg(),
        *members.toArg(),
    ),
) ?: 0

suspend fun ReThis.sScan(
    key: String,
    cursor: Long,
    vararg option: SScanOption,
): ScanResult<String> {
    val response = execute(mutableListOf("SSCAN".toArg(), key.toArg(), cursor.toArg()).writeArg(option))

    val arrResponse = response.safeCast<RArray>()?.value ?: exception { "Wrong response type" }
    val newCursor = arrResponse[0].unwrap<String>() ?: exception { "Missing cursor in response" }

    val membersArray = arrResponse[1].safeCast<RArray>()?.value ?: exception { "Missing members in response" }
    val members = membersArray.map { it.unwrap<String>() ?: exception { "Invalid member format" } }

    return ScanResult(cursor = newCursor, keys = members)
}

suspend fun ReThis.sUnion(vararg keys: String): Set<String> = execute<String>(
    listOf(
        "SUNION".toArg(),
        *keys.toArg(),
    ),
    isCollectionResponse = true,
)?.toSet() ?: emptySet()

suspend fun ReThis.sUnionStore(destination: String, vararg keys: String): Long = execute<Long>(
    listOf(
        "SUNIONSTORE".toArg(),
        destination.toArg(),
        *keys.toArg(),
    ),
) ?: 0

suspend fun ReThis.sInterCard(vararg keys: String, limit: Long? = null): Long = execute<Long>(
    mutableListOf(
        "SINTERCARD".toArg(),
        keys.size.toArg(),
        *keys.toArg(),
    ).apply {
        limit?.let { writeArg("LIMIT" to it) }
    },
) ?: 0

suspend fun ReThis.sMisMember(key: String, vararg members: String): List<Boolean> = execute<Long>(
    listOf(
        "SMISMEMBER".toArg(),
        key.toArg(),
        *members.toArg(),
    ),
    isCollectionResponse = true,
)?.map { it == 1L } ?: emptyList()
