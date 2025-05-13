package eu.vendeli.rethis.api.spec.commands.string

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("MSET", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface MSetCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        @RedisKey vararg kvPair: Pair<String, String>,
    ): CommandRequest<String>
}
