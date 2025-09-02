package eu.vendeli.rethis.api.spec.commands.bitmap

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("GETBIT", RedisOperation.READ, [RespCode.INTEGER])
fun interface GetBitCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        key: String,
        offset: Long
    ): CommandRequest
}
