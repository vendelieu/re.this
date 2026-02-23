package eu.vendeli.rethis.api.spec.commands.scripting

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.request.common.FlushType
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("FUNCTION FLUSH", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface FunctionFlushCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(flushType: FlushType): CommandRequest
}
