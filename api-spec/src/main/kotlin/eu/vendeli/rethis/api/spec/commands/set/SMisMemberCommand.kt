package eu.vendeli.rethis.api.spec.commands.set

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("SMISMEMBER", RedisOperation.READ, [RespCode.ARRAY])
fun interface SMisMemberCommand : RedisCommandSpec<List<Boolean>> {
    suspend fun encode(
        @RedisKey key: String,
        vararg member: String,
    ): CommandRequest<String>
}
