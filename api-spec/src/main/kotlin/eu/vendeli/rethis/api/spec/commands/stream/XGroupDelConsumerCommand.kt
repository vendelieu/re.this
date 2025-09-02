package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("XGROUP DELCONSUMER", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface XGroupDelConsumerCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        key: String,
        group: String,
        consumer: String
    ): CommandRequest
}
