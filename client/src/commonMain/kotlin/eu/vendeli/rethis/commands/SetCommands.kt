package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.types.processingException
import eu.vendeli.rethis.api.spec.common.types.RArray
import eu.vendeli.rethis.types.common.toArgument
import eu.vendeli.rethis.types.response.ScanResult
import eu.vendeli.rethis.types.options.SScanOption
import eu.vendeli.rethis.utils.safeCast
import eu.vendeli.rethis.utils.writeArgument
import kotlin.Long
import eu.vendeli.rethis.utils.execute
import eu.vendeli.rethis.utils.unwrap

suspend fun ReThis.sAdd(key: String, vararg members: String): Long = execute<Long>(
    listOf(
        "SADD".toArgument(),
        key.toArgument(),
        *members.toArgument(),
    ),
) ?: 0

suspend fun ReThis.sCard(key: String): Long = execute<Long>(
    listOf(
        "SCARD".toArgument(),
        key.toArgument(),
    ),
) ?: 0

suspend fun ReThis.sDiff(vararg keys: String): Set<String> = execute<String>(
    listOf(
        "SDIFF".toArgument(),
        *keys.toArgument(),
    ),
    isCollectionResponse = true,
)?.toSet() ?: emptySet()

suspend fun ReThis.sDiffStore(destination: String, vararg keys: String): Long = execute<Long>(
    listOf(
        "SDIFFSTORE".toArgument(),
        destination.toArgument(),
        *keys.toArgument(),
    ),
) ?: 0

suspend fun ReThis.sInter(vararg keys: String): Set<String> = execute<String>(
    listOf(
        "SINTER".toArgument(),
        *keys.toArgument(),
    ),
    isCollectionResponse = true,
)?.toSet() ?: emptySet()

suspend fun ReThis.sInterStore(destination: String, vararg keys: String): Long = execute<Long>(
    listOf(
        "SINTERSTORE".toArgument(),
        destination.toArgument(),
        *keys.toArgument(),
    ),
) ?: 0

suspend fun ReThis.sIsMember(key: String, member: String): Boolean = execute<Long>(
    listOf(
        "SISMEMBER".toArgument(),
        key.toArgument(),
        member.toArgument(),
    ),
) == 1L

suspend fun ReThis.sMembers(key: String): Set<String> = execute<String>(
    listOf(
        "SMEMBERS".toArgument(),
        key.toArgument(),
    ),
    isCollectionResponse = true,
)?.toSet() ?: emptySet()

suspend fun ReThis.sMove(source: String, destination: String, member: String): Boolean = execute<Long>(
    listOf(
        "SMOVE".toArgument(),
        source.toArgument(),
        destination.toArgument(),
        member.toArgument(),
    ),
) == 1L

suspend fun ReThis.sPop(
    key: String,
): String? = execute<String>(
    listOf("SPOP".toArgument(), key.toArgument()),
)

suspend fun ReThis.sPop(
    key: String,
    count: Long,
): Set<String> = execute<String>(
    listOf("SPOP".toArgument(), key.toArgument(), count.toArgument()),
    isCollectionResponse = true,
)?.toSet() ?: emptySet()

suspend fun ReThis.sRandMember(
    key: String,
): String? = execute<String>(
    listOf("SRANDMEMBER".toArgument(), key.toArgument()),
)

suspend fun ReThis.sRandMember(
    key: String,
    count: Long,
): List<String> = execute(
    listOf("SRANDMEMBER".toArgument(), key.toArgument(), count.toArgument()),
    isCollectionResponse = true,
) ?: emptyList()

suspend fun ReThis.sRem(key: String, vararg members: String): Long = execute<Long>(
    listOf(
        "SREM".toArgument(),
        key.toArgument(),
        *members.toArgument(),
    ),
) ?: 0

suspend fun ReThis.sScan(
    key: String,
    cursor: Long,
    vararg option: SScanOption,
): ScanResult<String> {
    val response =
        execute(mutableListOf("SSCAN".toArgument(), key.toArgument(), cursor.toArgument()).writeArgument(option))

    val arrResponse = response.safeCast<RArray>()?.value ?: processingException { "Wrong response type" }
    val newCursor = arrResponse[0].unwrap<String>() ?: processingException { "Missing cursor in response" }

    val membersArray = arrResponse[1].safeCast<RArray>()?.value ?: processingException { "Missing members in response" }
    val members = membersArray.map { it.unwrap<String>() ?: processingException { "Invalid member format" } }

    return ScanResult(cursor = newCursor, keys = members)
}

suspend fun ReThis.sUnion(vararg keys: String): Set<String> = execute<String>(
    listOf(
        "SUNION".toArgument(),
        *keys.toArgument(),
    ),
    isCollectionResponse = true,
)?.toSet() ?: emptySet()

suspend fun ReThis.sUnionStore(destination: String, vararg keys: String): Long = execute<Long>(
    listOf(
        "SUNIONSTORE".toArgument(),
        destination.toArgument(),
        *keys.toArgument(),
    ),
) ?: 0

suspend fun ReThis.sInterCard(vararg keys: String, limit: Long? = null): Long = execute<Long>(
    mutableListOf(
        "SINTERCARD".toArgument(),
        keys.size.toArgument(),
        *keys.toArgument(),
    ).apply {
        limit?.let { writeArgument("LIMIT" to it) }
    },
) ?: 0

suspend fun ReThis.sMisMember(key: String, vararg members: String): List<Boolean> = execute<Long>(
    listOf(
        "SMISMEMBER".toArgument(),
        key.toArgument(),
        *members.toArgument(),
    ),
    isCollectionResponse = true,
)?.map { it == 1L } ?: emptyList()
