package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.request.server.ReplicaOfArgs
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("REPLICAOF", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface ReplicaOfCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(args: ReplicaOfArgs): CommandRequest
}
