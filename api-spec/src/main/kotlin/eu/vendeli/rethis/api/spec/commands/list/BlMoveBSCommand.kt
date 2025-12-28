package eu.vendeli.rethis.api.spec.commands.list

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.response.common.MoveDirection
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode
import kotlinx.io.bytestring.ByteString

@RedisCommand(
    "BLMOVE",
    RedisOperation.WRITE,
    [RespCode.BULK, RespCode.NULL],
    isBlocking = true,
)
fun interface BlMoveBSCommand : RedisCommandSpec<ByteString> {
    suspend fun encode(
        source: String,
        destination: String,
        @RedisOption.Name("wherefrom") whereFrom: MoveDirection,
        @RedisOption.Name("whereto") whereTo: MoveDirection,
        timeout: Double,
    ): CommandRequest
}
