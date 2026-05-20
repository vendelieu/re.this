package eu.vendeli.rethis.api.spec.commands.timeseries

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("TS.DEL", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface TsDelCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        key: String,
        @RedisOption.Name("from_timestamp") fromTimestamp: Long,
        @RedisOption.Name("to_timestamp") toTimestamp: Long,
    ): CommandRequest
}
