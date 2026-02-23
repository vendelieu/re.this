package eu.vendeli.rethis.api.spec.commands.string

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("LCS", RedisOperation.READ, [RespCode.BULK])
fun interface LcsCommand : RedisCommandSpec<String> {
    suspend fun encode(
        key1: String,
        key2: String
    ): CommandRequest
}
