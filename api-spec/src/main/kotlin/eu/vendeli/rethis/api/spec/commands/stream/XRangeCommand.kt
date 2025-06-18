package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("XRANGE", RedisOperation.READ, [RespCode.ARRAY])
fun interface XRangeCommand : RedisCommandSpec<List<RType>> {
    suspend fun encode(
        key: String,
        start: String,
        end: String,
        @RedisOption.Token("COUNT") count: Long?,
    ): CommandRequest
}
