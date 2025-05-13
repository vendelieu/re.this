package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.XOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("XREVRANGE", RedisOperation.READ, [RespCode.ARRAY], extensions = [XOption.Limit::class])
fun interface XRevRangeCommand : RedisCommandSpec<List<RType>> {
    suspend fun encode(
        @RedisKey key: String,
        end: String,
        start: String,
        @RedisOptional limit: XOption.Limit?
    ): CommandRequest<String>
}
