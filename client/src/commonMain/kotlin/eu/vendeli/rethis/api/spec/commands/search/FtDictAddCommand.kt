package eu.vendeli.rethis.api.spec.commands.search

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("FT.DICTADD", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface FtDictAddCommand : RedisCommandSpec<Long> {
    suspend fun encode(dict: String, vararg term: String): CommandRequest
}
