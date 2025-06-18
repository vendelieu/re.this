package eu.vendeli.rethis.api.spec.commands.bitmap

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.request.bitmap.BitfieldOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("BITFIELD", RedisOperation.WRITE, [RespCode.ARRAY, RespCode.NULL])
fun interface BitfieldCommand : RedisCommandSpec<List<Long>> {
    suspend fun encode(
        key: String,
        vararg operation: BitfieldOption
    ): CommandRequest
}
