package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("XAUTOCLAIM", RedisOperation.WRITE, [RespCode.ARRAY])
fun interface XAutoClaimCommand : RedisCommandSpec<List<RType>> {
    suspend fun encode(
        @RedisKey key: String,
        group: String,
        consumer: String,
        minIdleTime: String,
        start: String,
        @RedisOptional @RedisOption.Token("COUNT") count: Long?,
        @RedisOptional @RedisOption.Token("JUSTID") justid: Boolean?
    ): CommandRequest<String>
}
