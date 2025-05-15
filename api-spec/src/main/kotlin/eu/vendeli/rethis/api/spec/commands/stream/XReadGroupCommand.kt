package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.XOption
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand(
    "XREADGROUP",
    RedisOperation.READ,
    [RespCode.ARRAY, RespCode.MAP, RespCode.NULL],
    isBlocking = true,
    extensions = [XOption.Limit::class],
)
fun interface XReadGroupCommand : RedisCommandSpec<Map<String, RType?>> {
    suspend fun encode(
        group: String,
        consumer: String,
//        @RedisKey keys: List<String>,
//        ids: List<String>,
        @RedisOptional count: XOption.Limit?,
        @RedisOptional milliseconds: Long?,
        @RedisOptional noack: Boolean?,
    ): CommandRequest<List<String>>
}
