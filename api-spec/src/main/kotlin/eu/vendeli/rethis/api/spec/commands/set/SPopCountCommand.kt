package eu.vendeli.rethis.api.spec.commands.set

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("SPOP", RedisOperation.WRITE, [RespCode.ARRAY])
fun interface SPopCountCommand : RedisCommandSpec<List<String>> {
    suspend fun encode(
        key: String,
        count: Long?,
    ): CommandRequest
}
