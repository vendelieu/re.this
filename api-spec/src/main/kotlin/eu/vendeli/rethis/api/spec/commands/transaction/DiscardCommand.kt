package eu.vendeli.rethis.api.spec.commands.transaction

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("DISCARD", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface DiscardCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(): CommandRequest<Nothing>
}
