package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.XOption
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand(
    "XREAD",
    RedisOperation.READ,
    [RespCode.ARRAY, RespCode.MAP, RespCode.NULL],
    isBlocking = true,
    extensions = [XOption.Limit::class],
)
fun interface XReadCommand : RedisCommandSpec<Map<String, RType?>> {
    suspend fun encode(
//        @RedisKey keys: List<String>,
//        ids: List<String>,
        @RedisOptional count: XOption.Limit?,
        @RedisOptional milliseconds: Long?,
    ): CommandRequest<List<String>>
}
