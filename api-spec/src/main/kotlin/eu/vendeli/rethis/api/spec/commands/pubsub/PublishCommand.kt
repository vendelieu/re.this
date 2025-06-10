package eu.vendeli.rethis.api.spec.commands.pubsub

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("PUBLISH", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface PublishCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        channel: String,
        message: String
    ): CommandRequest
}
