package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.request.stream.XReadGroupKeyIds
import eu.vendeli.rethis.api.spec.common.request.stream.XReadGroupOption
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand(
    "XREADGROUP",
    RedisOperation.WRITE,
    [RespCode.ARRAY, RespCode.MAP, RespCode.NULL],
    isBlocking = true,
)
fun interface XReadGroupCommand : RedisCommandSpec<Map<String, RType?>> {
    suspend fun encode(
        @RedisOption.Token("GROUP") group: String,
        consumer: String,
        keyIds: XReadGroupKeyIds,
        vararg option: XReadGroupOption,
    ): CommandRequest
}
