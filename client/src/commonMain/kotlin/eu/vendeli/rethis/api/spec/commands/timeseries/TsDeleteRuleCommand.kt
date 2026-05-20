package eu.vendeli.rethis.api.spec.commands.timeseries

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("TS.DELETERULE", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface TsDeleteRuleCommand : RedisCommandSpec<String> {
    suspend fun encode(
        @RedisOption.Name("sourceKey") sourceKey: String,
        @RedisOption.Name("destKey") destKey: String,
    ): CommandRequest
}
