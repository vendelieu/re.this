package eu.vendeli.rethis.api.spec.commands.timeseries

import eu.vendeli.rethis.shared.annotations.RIgnoreSpecAbsence
import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("TS.QUERYINDEX", RedisOperation.READ, [RespCode.ARRAY])
fun interface TsQueryIndexCommand : RedisCommandSpec<List<String>> {
    suspend fun encode(
        @RIgnoreSpecAbsence vararg filterExpr: String,
    ): CommandRequest
}
