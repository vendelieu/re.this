package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("ZSCORE", RedisOperation.READ, [RespCode.BULK, RespCode.DOUBLE, RespCode.NULL])
fun interface ZScoreCommand : RedisCommandSpec<Double> {
    suspend fun encode(
        key: String,
        member: String
    ): CommandRequest
}
