package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("XAUTOCLAIM", RedisOperation.WRITE, [RespCode.ARRAY])
fun interface XAutoClaimCommand : RedisCommandSpec<List<RType>> {
    suspend fun encode(
        @RedisKey key: String,
        group: String,
        consumer: String,
        minIdleTime: String,
        start: String,
        @RedisOptional count: Long?,
        @RedisOptional justid: Boolean?
    ): CommandRequest<String>
}
