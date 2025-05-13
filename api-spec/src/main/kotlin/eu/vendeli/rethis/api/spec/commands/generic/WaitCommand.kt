package eu.vendeli.rethis.api.spec.commands.generic

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("WAIT", RedisOperation.WRITE, [RespCode.INTEGER], isBlocking = true)
fun interface WaitCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        numreplicas: Long,
        timeout: Long
    ): CommandRequest<Nothing>
}
