package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("ZSCORE", RedisOperation.READ, [RespCode.BULK, RespCode.DOUBLE, RespCode.NULL])
fun interface ZScoreCommand : RedisCommandSpec<Double> {
    suspend fun encode(
        key: String,
        member: String
    ): CommandRequest
}
