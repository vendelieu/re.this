package eu.vendeli.rethis.api.spec.commands.pubsub

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation

@RedisMeta.SkipCommand
@RedisMeta.EnforcedKey
@RedisCommand("SUBSCRIBE", RedisOperation.READ, [])
fun interface SubscribeCommand : RedisCommandSpec<Unit> {
    suspend fun encode(
        @RedisMeta.EnforcedKey vararg channel: String,
    ): CommandRequest
}
