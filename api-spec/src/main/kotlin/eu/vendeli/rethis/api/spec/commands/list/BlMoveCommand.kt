package eu.vendeli.rethis.api.spec.commands.list

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.response.MoveDirection
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand(
    "BLMOVE",
    RedisOperation.WRITE,
    [RespCode.BULK, RespCode.NULL],
    isBlocking = true,
    extensions = [MoveDirection::class],
)
fun interface BlMoveCommand : RedisCommandSpec<String> {
    suspend fun encode(
        source: String,
        destination: String,
        @RedisOption.Name("wherefrom") whereFrom: MoveDirection,
        @RedisOption.Name("whereto")whereTo: MoveDirection,
        timeout: Double,
    ): CommandRequest
}
