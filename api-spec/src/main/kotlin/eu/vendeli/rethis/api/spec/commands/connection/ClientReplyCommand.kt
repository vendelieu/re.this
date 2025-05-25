package eu.vendeli.rethis.api.spec.commands.connection

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.request.ClientReplyMode
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("CLIENT REPLY", RedisOperation.WRITE, [RespCode.SIMPLE_STRING], extensions = [ClientReplyMode::class])
fun interface ClientReplyCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(action: ClientReplyMode): CommandRequest<Nothing>
}
