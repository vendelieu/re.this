package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("XACK", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface XAckCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        @RedisKey key: String,
        group: String,
        vararg id: String
    ): CommandRequest<String>
}
