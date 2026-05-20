package eu.vendeli.rethis.api.spec.commands.search

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("FT.ALIASADD", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface FtAliasAddCommand : RedisCommandSpec<String> {
    suspend fun encode(alias: String, index: String): CommandRequest
}
