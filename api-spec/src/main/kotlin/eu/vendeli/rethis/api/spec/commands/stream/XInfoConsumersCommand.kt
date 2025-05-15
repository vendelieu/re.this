package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("XINFO CONSUMERS", RedisOperation.READ, [RespCode.ARRAY])
fun interface XInfoConsumersCommand : RedisCommandSpec<List<RType>> {
    suspend fun encode(
        @RedisKey key: String,
        group: String
    ): CommandRequest<String>
}
