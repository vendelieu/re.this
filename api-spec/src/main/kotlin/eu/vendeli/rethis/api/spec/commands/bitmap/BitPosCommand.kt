package eu.vendeli.rethis.api.spec.commands.bitmap

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.bitmap.BitmapUnit
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("BITPOS", RedisOperation.READ, [RespCode.INTEGER], extensions = [BitmapUnit::class])
fun interface BitPosCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        @RedisKey key: String,
        bit: Long,
        @RedisOptional start: Long?,
        @RedisOptional end: Long?,
        @RedisOptional unit: BitmapUnit?
    ): CommandRequest<String>
}
