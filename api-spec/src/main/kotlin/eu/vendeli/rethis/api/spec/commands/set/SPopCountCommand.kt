package eu.vendeli.rethis.api.spec.commands.set

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("SPOP", RedisOperation.WRITE, [RespCode.ARRAY])
fun interface SPopCountCommand : RedisCommandSpec<Set<String>> {
    suspend fun encode(
        key: String,
        @RedisOptional count: Long?,
    ): CommandRequest
}
