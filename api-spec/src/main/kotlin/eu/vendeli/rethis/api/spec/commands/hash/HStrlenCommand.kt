package eu.vendeli.rethis.api.spec.commands.hash

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("HSTRLEN", RedisOperation.READ, [RespCode.INTEGER])
fun interface HStrlenCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        key: String,
        field: String
    ): CommandRequest
}
