package eu.vendeli.rethis.api.spec.commands.connection

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.connection.ClientUnblockType
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("CLIENT UNBLOCK", RedisOperation.WRITE, [RespCode.INTEGER], extensions = [ClientUnblockType::class])
fun interface ClientUnblockCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        clientId: Long,
        @RedisOptional unblockType: ClientUnblockType?
    ): CommandRequest<Nothing>
}
