package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
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
    extensions = [SaveSelector::class, ShutdownOptions::class],
)
fun interface ShutdownCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        @RedisOptional saveSelector: SaveSelector?,
        @RedisOptional vararg options: ShutdownOptions?,
    ): CommandRequest<Nothing>
}
