package eu.vendeli.rethis.api.spec.commands.pubsub

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation

@RedisMeta.SkipCommand
@RedisMeta.EnforcedKey
@RedisCommand("SSUBSCRIBE", RedisOperation.READ, [])
fun interface SSubscribeCommand : RedisCommandSpec<Unit> {
    suspend fun encode(
        @RedisMeta.EnforcedKey @RedisOption.Name("shardchannel") vararg shardChannel: String,
    ): CommandRequest
}
