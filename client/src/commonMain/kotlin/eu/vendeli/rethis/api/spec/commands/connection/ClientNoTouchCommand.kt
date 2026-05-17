package eu.vendeli.rethis.api.spec.commands.connection

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.request.connection.ClientNoTouchMode
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("CLIENT NO-TOUCH", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface ClientNoTouchCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(enabled: ClientNoTouchMode): CommandRequest
}
