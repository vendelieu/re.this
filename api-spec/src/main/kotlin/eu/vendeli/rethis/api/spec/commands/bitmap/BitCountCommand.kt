package eu.vendeli.rethis.api.spec.commands.bitmap

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.request.bitmap.BitmapDataType
import eu.vendeli.rethis.api.spec.common.request.bitmap.Range
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand(
    "BITCOUNT",
    RedisOperation.READ,
    [RespCode.INTEGER],
)
fun interface BitCountCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        key: String,
        range: Range?,
        unit: BitmapDataType?,
    ): CommandRequest
}
