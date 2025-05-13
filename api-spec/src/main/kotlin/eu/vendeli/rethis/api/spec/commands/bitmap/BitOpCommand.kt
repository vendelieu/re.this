package eu.vendeli.rethis.api.spec.commands.bitmap

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.request.BitOpOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("BITOP", RedisOperation.WRITE, [RespCode.INTEGER], extensions = [BitOpOption.OperationType::class])
fun interface BitOpCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        type: BitOpOption.OperationType,
        @RedisKey destkey: String,
        @RedisKey vararg key: String
    ): CommandRequest<List<String>>
}
