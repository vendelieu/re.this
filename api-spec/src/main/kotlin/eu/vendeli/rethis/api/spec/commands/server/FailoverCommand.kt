package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.shared.annotations.RIgnoreSpecAbsence
import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.request.server.FailoverOptions
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("FAILOVER", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface FailoverCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        @RIgnoreSpecAbsence vararg option: FailoverOptions,
    ): CommandRequest
}
