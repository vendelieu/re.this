package eu.vendeli.rethis.api.spec.commands.connection

import eu.vendeli.rethis.shared.annotations.RIgnoreSpecAbsence
import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.request.connection.ClientStandby
import eu.vendeli.rethis.shared.request.connection.ClientTrackingMode
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand(
    "CLIENT TRACKING",
    RedisOperation.WRITE,
    [RespCode.SIMPLE_STRING],
)
fun interface ClientTrackingCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        status: ClientStandby,
        @RIgnoreSpecAbsence vararg options: ClientTrackingMode,
    ): CommandRequest
}
