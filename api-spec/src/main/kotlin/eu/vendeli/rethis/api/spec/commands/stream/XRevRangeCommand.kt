package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.types.*

@RedisCommand("XREVRANGE", RedisOperation.READ, [RespCode.ARRAY])
fun interface XRevRangeCommand : RedisCommandSpec<List<RType>> {
    suspend fun encode(
        key: String,
        end: String,
        start: String,
        @RedisOption.Token("COUNT") count: Long?,
    ): CommandRequest
}
