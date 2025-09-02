package eu.vendeli.rethis.api.spec.commands.scripting

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("FUNCTION LOAD", RedisOperation.WRITE, [RespCode.BULK])
fun interface FunctionLoadCommand : RedisCommandSpec<String> {
    suspend fun encode(
        functionCode: String,
        @RedisOption.Token("REPLACE") replace: Boolean?,
    ): CommandRequest
}
