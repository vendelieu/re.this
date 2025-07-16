package eu.vendeli.rethis.api.spec.commands.pubsub

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisMeta.EnforcedKey
@RedisCommand("SPUBLISH", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface SPublishCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        @RedisMeta.EnforcedKey shardchannel: String,
        message: String
    ): CommandRequest
}
