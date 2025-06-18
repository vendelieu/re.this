package eu.vendeli.rethis.api.spec.commands.scripting

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("FUNCTION LOAD", RedisOperation.WRITE, [RespCode.BULK])
fun interface FunctionLoadCommand : RedisCommandSpec<String> {
    suspend fun encode(
        functionCode: String,
        @RedisOption.Token("REPLACE") replace: Boolean?,
    ): CommandRequest
}
