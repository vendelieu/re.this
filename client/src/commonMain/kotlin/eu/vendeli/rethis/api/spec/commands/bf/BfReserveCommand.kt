package eu.vendeli.rethis.api.spec.commands.bf

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("BF.RESERVE", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface BfReserveCommand : RedisCommandSpec<String> {
    suspend fun encode(
        key: String,
        @RedisOption.Name("error_rate") errorRate: Double,
        capacity: Long,
        @RedisOption.Token("EXPANSION") expansion: Long?,
        @RedisOption.Token("NONSCALING") nonscaling: Boolean?,
    ): CommandRequest
}
