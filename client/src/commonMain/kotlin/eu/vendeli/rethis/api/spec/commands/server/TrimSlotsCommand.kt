package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.shared.annotations.RIgnoreSpecAbsence
import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.request.server.TrimSlotsOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("TRIMSLOTS", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface TrimSlotsCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        @RIgnoreSpecAbsence vararg options: TrimSlotsOption,
    ): CommandRequest
}
