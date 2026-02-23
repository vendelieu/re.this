package eu.vendeli.rethis.api.spec.commands.transaction

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("DISCARD", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface DiscardCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(): CommandRequest
}
