package eu.vendeli.rethis.api.spec.commands.search

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("FT.SUGDEL", RedisOperation.WRITE, [RespCode.INTEGER, RespCode.BOOLEAN])
fun interface FtSugDelCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(key: String, string: String): CommandRequest
}
