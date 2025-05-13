package eu.vendeli.rethis.api.spec.commands.list

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.response.MoveDirection
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand(
    "BLMOVE",
    RedisOperation.READ,
    [RespCode.BULK, RespCode.NULL],
    isBlocking = true,
    extensions = [MoveDirection::class],
)
fun interface BlMoveCommand : RedisCommandSpec<String> {
    suspend fun encode(
        @RedisKey source: String,
        @RedisKey destination: String,
        moveFrom: MoveDirection,
        moveTo: MoveDirection,
        timeout: Double,
    ): CommandRequest<List<String>>
}
