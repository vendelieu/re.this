package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.request.common.UpdateStrategyOption
import eu.vendeli.rethis.shared.response.stream.ZMember
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand(
    "ZADD",
    RedisOperation.WRITE,
    [RespCode.DOUBLE, RespCode.BULK, RespCode.NULL],
)
fun interface ZAddExtendedCommand : RedisCommandSpec<Double> {
    suspend fun encode(
        key: String,
        vararg data: ZMember,
        condition: UpdateStrategyOption.ExistenceRule?,
        comparison: UpdateStrategyOption.ComparisonRule?,
        @RedisOption.Token("CH") change: Boolean?,
        @RedisOption.Token("INCR") increment: Boolean?,
    ): CommandRequest
}
