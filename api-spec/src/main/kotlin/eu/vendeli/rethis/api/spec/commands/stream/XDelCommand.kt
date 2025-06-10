package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("XDEL", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface XDelCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        key: String,
        vararg id: String
    ): CommandRequest
}
