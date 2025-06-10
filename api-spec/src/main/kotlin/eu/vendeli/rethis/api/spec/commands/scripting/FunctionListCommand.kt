package eu.vendeli.rethis.api.spec.commands.scripting

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("FUNCTION LIST", RedisOperation.READ, [RespCode.ARRAY])
fun interface FunctionListCommand : RedisCommandSpec<List<RType>> {
    suspend fun encode(
        @RedisOptional @RedisOption.Token("LIBRARYNAME") libraryNamePattern: String?,
        @RedisOptional @RedisOption.Token("WITHCODE") withCode: Boolean?
    ): CommandRequest
}
