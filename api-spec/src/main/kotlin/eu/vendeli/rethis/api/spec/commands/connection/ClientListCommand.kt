package eu.vendeli.rethis.api.spec.commands.connection

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.request.connection.ClientType
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("CLIENT LIST", RedisOperation.READ, [RespCode.BULK])
fun interface ClientListCommand : RedisCommandSpec<String> {
    suspend fun encode(
        clientType: ClientType?,
        @RedisOption.Token("ID") vararg clientId: Long,
    ): CommandRequest
}
