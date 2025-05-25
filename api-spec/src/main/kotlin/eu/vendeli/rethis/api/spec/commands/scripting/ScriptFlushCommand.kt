package eu.vendeli.rethis.api.spec.commands.scripting

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.common.FlushType
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("SCRIPT FLUSH", RedisOperation.WRITE, [RespCode.SIMPLE_STRING], extensions = [FlushType::class])
fun interface ScriptFlushCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(@RedisOptional flushType: FlushType?): CommandRequest<Nothing>
}
