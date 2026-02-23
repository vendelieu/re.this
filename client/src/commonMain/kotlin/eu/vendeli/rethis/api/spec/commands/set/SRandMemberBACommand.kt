package eu.vendeli.rethis.api.spec.commands.set

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("SRANDMEMBER", RedisOperation.READ, [RespCode.BULK])
fun interface SRandMemberBACommand : RedisCommandSpec<ByteArray> {
    suspend fun encode(key: String): CommandRequest
}
