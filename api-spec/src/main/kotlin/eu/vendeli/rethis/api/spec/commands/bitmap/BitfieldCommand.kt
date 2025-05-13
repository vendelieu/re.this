package eu.vendeli.rethis.api.spec.commands.bitmap

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.request.BitfieldOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("BITFIELD", RedisOperation.WRITE, [RespCode.ARRAY, RespCode.NULL], extensions = [BitfieldOption::class])
fun interface BitfieldCommand : RedisCommandSpec<List<Long>> {
    suspend fun encode(
        @RedisKey key: String,
        vararg options: BitfieldOption
    ): CommandRequest<String>
}
