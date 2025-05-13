package eu.vendeli.rethis.api.spec.commands.scripting

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("SCRIPT KILL", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface ScriptKillCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(): CommandRequest<Nothing>
}
