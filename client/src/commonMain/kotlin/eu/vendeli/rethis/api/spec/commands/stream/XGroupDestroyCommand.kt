package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("XGROUP DESTROY", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface XGroupDestroyCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        key: String,
        group: String
    ): CommandRequest
}
