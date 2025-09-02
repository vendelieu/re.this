package eu.vendeli.rethis.api.spec.commands.connection

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("SELECT", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface SelectCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        index: Long
    ): CommandRequest
}
