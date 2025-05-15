package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("XREVRANGE", RedisOperation.READ, [RespCode.ARRAY])
fun interface XRevRangeCommand : RedisCommandSpec<List<RType>> {
    suspend fun encode(
        @RedisKey key: String,
        end: String,
        start: String,
        @RedisOptional @RedisOption.Token("COUNT") count: Long?,
    ): CommandRequest<String>
}
