package eu.vendeli.rethis.api.spec.commands.pubsub

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation

@RedisCommand("UNSUBSCRIBE", RedisOperation.WRITE, [])
fun interface UnsubscribeCommand : RedisCommandSpec<Unit> {
    suspend fun encode(vararg channel: String): CommandRequest
}
