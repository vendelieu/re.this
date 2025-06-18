package eu.vendeli.rethis.api.spec.commands.set

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("SRANDMEMBER", RedisOperation.READ, [RespCode.ARRAY])
fun interface SRandMemberCountCommand : RedisCommandSpec<List<String>> {
    suspend fun encode(
        key: String,
        count: Long?,
    ): CommandRequest
}
