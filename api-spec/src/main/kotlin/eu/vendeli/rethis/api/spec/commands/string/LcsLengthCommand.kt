package eu.vendeli.rethis.api.spec.commands.string

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.request.string.LcsMode
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand(
    "LCS", 
    RedisOperation.READ,
    [RespCode.INTEGER],
    extensions = [LcsMode.LEN::class]
)
fun interface LcsLengthCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        @RedisKey key1: String,
        @RedisKey key2: String,
        mode: LcsMode.LEN
    ): CommandRequest<List<String>>
}
