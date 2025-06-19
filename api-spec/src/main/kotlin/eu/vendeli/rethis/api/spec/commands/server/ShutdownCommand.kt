package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.api.spec.common.annotations.RIgnoreSpecAbsence
import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.request.server.SaveSelector
import eu.vendeli.rethis.api.spec.common.request.server.ShutdownOptions
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

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
