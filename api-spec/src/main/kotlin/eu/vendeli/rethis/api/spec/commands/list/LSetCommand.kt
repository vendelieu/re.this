package eu.vendeli.rethis.api.spec.commands.list

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("LSET", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface LSetCommand : RedisCommandSpec<String> {
    suspend fun encode(
        key: String,
        index: Long,
        element: String
    ): CommandRequest
}
