package eu.vendeli.rethis.api.spec.commands.timeseries

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.request.timeseries.TsSample
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("TS.MADD", RedisOperation.WRITE, [RespCode.ARRAY])
fun interface TsMAddCommand : RedisCommandSpec<List<Long>> {
    suspend fun encode(vararg ktv: TsSample): CommandRequest
}
