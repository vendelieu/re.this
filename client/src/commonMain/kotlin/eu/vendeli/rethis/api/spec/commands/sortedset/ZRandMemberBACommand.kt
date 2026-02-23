package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("ZRANDMEMBER", RedisOperation.READ, [RespCode.BULK])
fun interface ZRandMemberBACommand : RedisCommandSpec<ByteArray> {
    suspend fun encode(key: String): CommandRequest
}
