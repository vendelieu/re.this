package eu.vendeli.rethis.api.spec.commands.bitmap

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.request.bitmap.BitmapUnit
import eu.vendeli.rethis.shared.request.bitmap.Range
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand(
    "BITCOUNT",
    RedisOperation.READ,
    [RespCode.INTEGER],
)
fun interface BitCountCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        key: String,
        range: Range?,
        unit: BitmapUnit?,
    ): CommandRequest
}
