package eu.vendeli.rethis.api.spec.commands.pubsub

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation

@RedisMeta.SkipCommand
@RedisCommand("SSUBSCRIBE", RedisOperation.READ, [])
fun interface SSubscribeCommand : RedisCommandSpec<Unit> {
    suspend fun encode(
        @RedisOption.Name("shardchannel") vararg shardChannel: String,
    ): CommandRequest
}
