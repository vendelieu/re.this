package eu.vendeli.rethis.api.spec.commands.pubsub

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation

@RedisCommand("SUNSUBSCRIBE", RedisOperation.WRITE, [])
fun interface SUnsubscribeCommand : RedisCommandSpec<Unit> {
    suspend fun encode(vararg shardchannel: String): CommandRequest
}
