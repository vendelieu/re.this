package eu.vendeli.rethis.api.spec.commands.list

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("LSET", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface LSetCommand : RedisCommandSpec<String> {
    suspend fun encode(
        key: String,
        index: Long,
        element: String
    ): CommandRequest
}
