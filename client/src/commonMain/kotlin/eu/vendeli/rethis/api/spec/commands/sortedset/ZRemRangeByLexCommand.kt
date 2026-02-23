package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("ZREMRANGEBYLEX", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface ZRemRangeByLexCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        key: String,
        min: String,
        max: String
    ): CommandRequest
}
