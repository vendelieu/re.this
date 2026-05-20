package eu.vendeli.rethis.api.spec.commands.search

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("FT.SUGADD", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface FtSugAddCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        key: String,
        string: String,
        score: Double,
        @RedisOption.Token("INCR") incr: Boolean?,
        @RedisOption.Token("PAYLOAD") payload: String?,
    ): CommandRequest
}
