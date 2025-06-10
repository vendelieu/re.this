package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("ZREMRANGEBYLEX", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface ZRemRangeByLexCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        key: String,
        min: String,
        max: String
    ): CommandRequest
}
