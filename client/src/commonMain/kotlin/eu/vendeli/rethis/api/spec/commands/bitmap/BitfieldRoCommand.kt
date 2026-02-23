package eu.vendeli.rethis.api.spec.commands.bitmap

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.request.bitmap.BitfieldOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("BITFIELD_RO", RedisOperation.READ, [RespCode.ARRAY])
fun interface BitfieldRoCommand : RedisCommandSpec<List<Long>> {
    suspend fun encode(
        key: String,
        @RedisOption.Name("getBlock") vararg options: BitfieldOption.Get
    ): CommandRequest
}
