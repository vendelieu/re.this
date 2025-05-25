package eu.vendeli.rethis.api.spec.commands.list

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.response.MoveDirection
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("LMOVE", RedisOperation.WRITE, [RespCode.BULK], extensions = [MoveDirection::class])
fun interface LMoveCommand : RedisCommandSpec<String> {
    suspend fun encode(
        @RedisKey source: String,
        @RedisKey destination: String,
        @RedisOption.Name("wherefrom") whereFrom: MoveDirection,
        @RedisOption.Name("whereto") whereTo: MoveDirection
    ): CommandRequest<List<String>>
}
