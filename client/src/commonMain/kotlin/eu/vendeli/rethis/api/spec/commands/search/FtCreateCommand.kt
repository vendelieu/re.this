package eu.vendeli.rethis.api.spec.commands.search

import eu.vendeli.rethis.shared.annotations.RIgnoreSpecAbsence
import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("FT.CREATE", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface FtCreateCommand : RedisCommandSpec<String> {
    suspend fun encode(
        index: String,
        @RIgnoreSpecAbsence vararg args: String,
    ): CommandRequest
}
