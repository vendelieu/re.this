package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.common.UpdateStrategyOption
import eu.vendeli.rethis.api.spec.common.response.ZMember
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand(
    "ZADD",
    RedisOperation.WRITE,
    [RespCode.DOUBLE, RespCode.BULK, RespCode.NULL],
    extensions = [ZMember::class, UpdateStrategyOption.ExistenceRule::class, UpdateStrategyOption.ComparisonRule::class],
)
fun interface ZAddExtendedCommand : RedisCommandSpec<Double> {
    suspend fun encode(
        key: String,
        vararg data: ZMember,
        @RedisOptional condition: UpdateStrategyOption.ExistenceRule?,
        @RedisOptional comparison: UpdateStrategyOption.ComparisonRule?,
        @RedisOptional @RedisOption.Token("CH") change: Boolean?,
        @RedisOptional @RedisOption.Token("INCR") increment: Boolean?,
    ): CommandRequest
}
