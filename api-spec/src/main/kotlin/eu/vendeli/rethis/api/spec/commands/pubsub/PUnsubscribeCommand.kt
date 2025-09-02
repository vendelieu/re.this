package eu.vendeli.rethis.api.spec.commands.pubsub

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation

@RedisCommand("PUNSUBSCRIBE", RedisOperation.WRITE, [])
fun interface PUnsubscribeCommand : RedisCommandSpec<Unit> {
    suspend fun encode(vararg pattern: String): CommandRequest
}
