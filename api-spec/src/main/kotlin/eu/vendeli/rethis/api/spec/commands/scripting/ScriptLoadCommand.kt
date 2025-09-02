package eu.vendeli.rethis.api.spec.commands.scripting

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("SCRIPT LOAD", RedisOperation.WRITE, [RespCode.BULK])
fun interface ScriptLoadCommand : RedisCommandSpec<String> {
    suspend fun encode(script: String): CommandRequest
}
