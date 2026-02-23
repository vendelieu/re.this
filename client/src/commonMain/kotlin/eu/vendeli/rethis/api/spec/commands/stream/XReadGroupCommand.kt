package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.shared.annotations.RIgnoreSpecAbsence
import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.request.stream.XReadGroupKeyIds
import eu.vendeli.rethis.shared.request.stream.XReadGroupOption
import eu.vendeli.rethis.shared.types.*

@RedisCommand(
    "XREADGROUP",
    RedisOperation.WRITE,
    [RespCode.ARRAY, RespCode.MAP, RespCode.NULL],
    isBlocking = true,
)
fun interface XReadGroupCommand : RedisCommandSpec<Map<String, RType>> {
    suspend fun encode(
        @RedisOption.Token("GROUP") group: String,
        consumer: String,
        streams: XReadGroupKeyIds,
        @RIgnoreSpecAbsence vararg option: XReadGroupOption,
    ): CommandRequest
}
