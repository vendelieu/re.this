package eu.vendeli.rethis.api.spec.commands.bitmap

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("SETBIT", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface SetBitCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        @RedisKey key: String,
        offset: Long,
        value: Long
    ): CommandRequest<String>
}
