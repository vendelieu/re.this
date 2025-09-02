package eu.vendeli.rethis.api.spec.commands.pubsub

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("SPUBLISH", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface SPublishCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        shardchannel: String,
        message: String
    ): CommandRequest
}
