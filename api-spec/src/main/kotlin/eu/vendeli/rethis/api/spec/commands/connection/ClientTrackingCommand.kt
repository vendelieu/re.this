package eu.vendeli.rethis.api.spec.commands.connection

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.connection.ClientStandby
import eu.vendeli.rethis.api.spec.common.request.connection.ClientTrackingMode
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand(
    "CLIENT TRACKING",
    RedisOperation.WRITE,
    [RespCode.SIMPLE_STRING],
    extensions = [ClientStandby::class, ClientTrackingMode::class],
)
fun interface ClientTrackingCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        status: ClientStandby,
        @RedisOptional vararg options: ClientTrackingMode,
    ): CommandRequest
}
