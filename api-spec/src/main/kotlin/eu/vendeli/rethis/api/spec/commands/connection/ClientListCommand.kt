package eu.vendeli.rethis.api.spec.commands.connection

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.ClientType
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("CLIENT LIST", RedisOperation.READ, [RespCode.BULK], extensions = [ClientType::class])
fun interface ClientListCommand : RedisCommandSpec<String> {
    suspend fun encode(
        @RedisOptional type: ClientType?,
        @RedisOptional @RedisOption.Name("ID") vararg clientId: Long,
    ): CommandRequest<Nothing>
}
