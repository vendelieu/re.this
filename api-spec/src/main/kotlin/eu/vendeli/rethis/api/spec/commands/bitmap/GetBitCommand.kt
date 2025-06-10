package eu.vendeli.rethis.api.spec.commands.bitmap

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("GETBIT", RedisOperation.READ, [RespCode.INTEGER])
fun interface GetBitCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        key: String,
        offset: Long
    ): CommandRequest
}
