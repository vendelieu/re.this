package eu.vendeli.rethis.api.spec.commands.bitmap

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.request.bitmap.BitfieldOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("BITFIELD", RedisOperation.WRITE, [RespCode.ARRAY, RespCode.NULL])
fun interface BitfieldCommand : RedisCommandSpec<List<Long>> {
    suspend fun encode(
        key: String,
        vararg operation: BitfieldOption
    ): CommandRequest
}
