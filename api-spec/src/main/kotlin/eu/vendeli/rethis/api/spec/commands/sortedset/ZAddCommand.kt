package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.request.common.UpdateStrategyOption
import eu.vendeli.rethis.api.spec.common.response.stream.ZMember
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand(
    "ZADD",
    RedisOperation.WRITE,
    [RespCode.INTEGER, RespCode.NULL],
)
fun interface ZAddCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        key: String,
        vararg data: ZMember,
        condition: UpdateStrategyOption.ExistenceRule?,
        comparison: UpdateStrategyOption.ComparisonRule?,
        @RedisOption.Token("CH") change: Boolean?,
    ): CommandRequest
}
