package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.shared.annotations.RIgnoreSpecAbsence
import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.request.server.SaveSelector
import eu.vendeli.rethis.shared.request.server.ShutdownOptions
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand(
    "SHUTDOWN",
    RedisOperation.WRITE,
    [RespCode.SIMPLE_STRING],
)
fun interface ShutdownCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        saveSelector: SaveSelector?,
        @RIgnoreSpecAbsence vararg options: ShutdownOptions,
    ): CommandRequest
}
