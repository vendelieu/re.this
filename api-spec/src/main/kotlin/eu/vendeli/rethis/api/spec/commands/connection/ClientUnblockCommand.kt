package eu.vendeli.rethis.api.spec.commands.connection

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.request.connection.ClientUnblockType
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("CLIENT UNBLOCK", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface ClientUnblockCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        clientId: Long,
        unblockType: ClientUnblockType?
    ): CommandRequest
}
