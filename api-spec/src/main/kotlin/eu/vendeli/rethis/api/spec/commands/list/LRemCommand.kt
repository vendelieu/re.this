package eu.vendeli.rethis.api.spec.commands.list

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("LREM", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface LRemCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        key: String,
        count: Long,
        element: String
    ): CommandRequest
}
