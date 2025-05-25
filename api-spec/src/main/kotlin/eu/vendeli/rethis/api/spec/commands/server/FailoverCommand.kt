package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.server.FailoverOptions
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("FAILOVER", RedisOperation.WRITE, [RespCode.SIMPLE_STRING], extensions = [FailoverOptions::class])
fun interface FailoverCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        @RedisOptional vararg option: FailoverOptions,
    ): CommandRequest<Nothing>
}
