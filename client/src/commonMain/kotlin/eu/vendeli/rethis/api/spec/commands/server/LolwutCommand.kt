package eu.vendeli.rethis.api.spec.commands.server

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("LOLWUT", RedisOperation.READ, [RespCode.BULK, RespCode.VERBATIM_STRING])
fun interface LolwutCommand : RedisCommandSpec<String> {
    suspend fun encode(
        @RedisOption.Token("VERSION") version: Long?,
    ): CommandRequest
}
