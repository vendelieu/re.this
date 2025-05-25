package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.stream.XReadGroupKeyIds
import eu.vendeli.rethis.api.spec.common.request.stream.XReadGroupOption
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand(
    "XREADGROUP",
    RedisOperation.READ,
    [RespCode.ARRAY, RespCode.MAP, RespCode.NULL],
    isBlocking = true,
    extensions = [XReadGroupKeyIds::class, XReadGroupOption::class]
)
fun interface XReadGroupCommand : RedisCommandSpec<Map<String, RType?>> {
    suspend fun encode(
        @RedisOption.Token("GROUP") group: String,
        consumer: String,
        keyIds: XReadGroupKeyIds,
        @RedisOptional vararg option: XReadGroupOption,
    ): CommandRequest<List<String>>
}
