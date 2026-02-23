package eu.vendeli.rethis.api.spec.commands.pubsub

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("PUBLISH", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface PublishCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        channel: String,
        message: String
    ): CommandRequest
}
