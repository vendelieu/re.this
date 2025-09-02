package eu.vendeli.rethis.api.spec.commands.bitmap

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.request.bitmap.BitOpOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("BITOP", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface BitOpCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        operation: BitOpOption.OperationType,
        destkey: String,
        vararg key: String
    ): CommandRequest
}
