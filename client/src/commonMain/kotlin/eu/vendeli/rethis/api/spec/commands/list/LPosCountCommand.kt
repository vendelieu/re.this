package eu.vendeli.rethis.api.spec.commands.list

import eu.vendeli.rethis.shared.annotations.RIgnoreSpecAbsence
import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.request.list.LPosOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand(
    "LPOS",
    RedisOperation.READ,
    [RespCode.ARRAY],
)
fun interface LPosCountCommand : RedisCommandSpec<List<Long>> {
    suspend fun encode(
        key: String,
        element: String,
        @RedisOption.Token("COUNT") numMatches: Long,
        @RIgnoreSpecAbsence vararg option: LPosOption,
    ): CommandRequest
}
