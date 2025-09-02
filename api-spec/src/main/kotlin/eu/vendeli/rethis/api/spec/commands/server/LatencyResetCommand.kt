package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("LATENCY RESET", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface LatencyResetCommand : RedisCommandSpec<Long> {
    suspend fun encode(vararg event: String): CommandRequest
}
