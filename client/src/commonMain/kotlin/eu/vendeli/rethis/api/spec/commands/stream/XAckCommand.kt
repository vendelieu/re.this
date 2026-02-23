package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("XACK", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface XAckCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        key: String,
        group: String,
        vararg id: String
    ): CommandRequest
}
