package eu.vendeli.rethis.api.spec.commands.bitmap

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.request.bitmap.BitfieldOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("BITFIELD_RO", RedisOperation.READ, [RespCode.ARRAY])
fun interface BitfieldRoCommand : RedisCommandSpec<List<Long>> {
    suspend fun encode(
        key: String,
        @RedisOption.Name("getBlock") vararg options: BitfieldOption.Get
    ): CommandRequest
}
