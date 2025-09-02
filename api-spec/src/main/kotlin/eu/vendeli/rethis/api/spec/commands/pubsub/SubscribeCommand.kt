package eu.vendeli.rethis.api.spec.commands.pubsub

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation

@RedisMeta.SkipCommand
@RedisCommand("SUBSCRIBE", RedisOperation.READ, [])
fun interface SubscribeCommand : RedisCommandSpec<Unit> {
    suspend fun encode(
        vararg channel: String,
    ): CommandRequest
}
