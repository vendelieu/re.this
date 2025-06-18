package eu.vendeli.rethis.api.spec.commands.bitmap

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.request.bitmap.BitOpOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("BITOP", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface BitOpCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        operation: BitOpOption.OperationType,
        destkey: String,
        vararg key: String
    ): CommandRequest
}
