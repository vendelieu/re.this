package eu.vendeli.rethis.api.spec.commands.generic

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("WAIT", RedisOperation.WRITE, [RespCode.INTEGER], isBlocking = true)
fun interface WaitCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        numreplicas: Long,
        timeout: Long
    ): CommandRequest
}
