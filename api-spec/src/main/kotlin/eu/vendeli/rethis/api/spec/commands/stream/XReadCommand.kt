package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.XOption
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand(
    "XREAD",
    RedisOperation.READ,
    [RespCode.ARRAY, RespCode.MAP, RespCode.NULL],
    isBlocking = true,
)
fun interface XReadCommand : RedisCommandSpec<Map<String, RType?>> {
    suspend fun encode(
        @RedisKey @RedisOption.Token("STREAMS") key: List<String>,
        id: List<String>,
        @RedisOptional @RedisOption.Token("COUNT") count: Long?,
        @RedisOptional @RedisOption.Token("BLOCK") milliseconds: Long?,
    ): CommandRequest<List<String>>
}
