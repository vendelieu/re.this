package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.types.*

@RedisCommand(
    "XREAD",
    RedisOperation.READ,
    [RespCode.ARRAY, RespCode.MAP, RespCode.NULL],
    isBlocking = true,
)
fun interface XReadCommand : RedisCommandSpec<Map<String, RType>> {
    suspend fun encode(
        @RedisOption.Token("STREAMS") key: List<String>,
        id: List<String>,
        @RedisOption.Token("COUNT") count: Long?,
        @RedisOption.Token("BLOCK") milliseconds: Long?,
    ): CommandRequest
}
