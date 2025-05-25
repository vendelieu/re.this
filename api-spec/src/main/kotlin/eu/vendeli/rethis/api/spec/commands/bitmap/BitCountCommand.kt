package eu.vendeli.rethis.api.spec.commands.bitmap

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.BitmapDataType
import eu.vendeli.rethis.api.spec.common.request.Range
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand(
    "BITCOUNT",
    RedisOperation.READ,
    [RespCode.INTEGER],
    extensions = [Range::class, BitmapDataType::class],
)
fun interface BitCountCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        @RedisKey key: String,
        @RedisOptional range: Range?,
        @RedisOptional unit: BitmapDataType?,
    ): CommandRequest<String>
}
