package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.*

@RedisCommand("XINFO CONSUMERS", RedisOperation.READ, [RespCode.ARRAY])
fun interface XInfoConsumersCommand : RedisCommandSpec<List<RType>> {
    suspend fun encode(
        key: String,
        group: String
    ): CommandRequest
}
