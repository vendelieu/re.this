package eu.vendeli.rethis.api.spec.commands.connection

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("CLIENT GETNAME", RedisOperation.READ, [RespCode.BULK, RespCode.NULL])
fun interface ClientGetNameCommand : RedisCommandSpec<String> {
    suspend fun encode(): CommandRequest
}
