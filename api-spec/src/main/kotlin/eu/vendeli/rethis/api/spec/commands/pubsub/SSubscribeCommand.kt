package eu.vendeli.rethis.api.spec.commands.pubsub

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation

@RedisMeta.SkipCommand
@RedisCommand("SSUBSCRIBE", RedisOperation.READ, [])
fun interface SSubscribeCommand : RedisCommandSpec<Unit> {
    suspend fun encode(
        @RedisOption.Name("shardchannel") vararg shardChannel: String,
    ): CommandRequest
}
