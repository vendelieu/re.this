package eu.vendeli.rethis.api.spec.commands.scripting

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.request.scripting.ScriptDebugMode
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("SCRIPT DEBUG", RedisOperation.WRITE, [RespCode.SIMPLE_STRING], extensions = [ScriptDebugMode::class])
fun interface ScriptDebugCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(mode: ScriptDebugMode): CommandRequest
}
