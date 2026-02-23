package eu.vendeli.rethis.api.spec.commands.connection

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.request.connection.ClientType
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("CLIENT LIST", RedisOperation.READ, [RespCode.BULK])
fun interface ClientListCommand : RedisCommandSpec<String> {
    suspend fun encode(
        clientType: ClientType?,
        @RedisOption.Token("ID") vararg clientId: Long,
    ): CommandRequest
}
