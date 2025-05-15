package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("FAILOVER", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface FailoverCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        @RedisOptional timeout: Long?,
        @RedisOptional force: Boolean?,
        @RedisOptional abort: Boolean?,
    ): CommandRequest<Nothing>
}
