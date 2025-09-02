package eu.vendeli.rethis.api.spec.commands.string

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("MGET", RedisOperation.READ, [RespCode.ARRAY])
fun interface MGetCommand : RedisCommandSpec<List<String?>> {
    suspend fun encode(
        vararg key: String,
    ): CommandRequest
}
