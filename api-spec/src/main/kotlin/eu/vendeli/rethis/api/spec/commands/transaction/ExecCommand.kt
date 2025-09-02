package eu.vendeli.rethis.api.spec.commands.transaction

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.*

@RedisCommand("EXEC", RedisOperation.WRITE, [RespCode.ARRAY, RespCode.NULL])
fun interface ExecCommand : RedisCommandSpec<List<RType>> {
    suspend fun encode(): CommandRequest
}
