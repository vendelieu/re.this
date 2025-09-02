package eu.vendeli.rethis.api.spec.commands.pubsub

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("PUBSUB SHARDCHANNELS", RedisOperation.READ, [RespCode.ARRAY])
fun interface PubSubShardChannelsCommand : RedisCommandSpec<List<String>> {
    suspend fun encode(
        pattern: String?
    ): CommandRequest
}
