package eu.vendeli.rethis.api.spec.commands.scripting

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.types.*

@RedisCommand("FUNCTION LIST", RedisOperation.READ, [RespCode.ARRAY])
fun interface FunctionListCommand : RedisCommandSpec<List<RType>> {
    suspend fun encode(
        @RedisOption.Token("LIBRARYNAME") libraryNamePattern: String?,
        @RedisOption.Token("WITHCODE") @RedisOption.Name("withcode") withCode: Boolean?
    ): CommandRequest
}
