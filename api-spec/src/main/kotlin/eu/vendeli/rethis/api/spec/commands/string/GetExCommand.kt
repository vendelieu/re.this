package eu.vendeli.rethis.api.spec.commands.string

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.request.string.GetExOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand(
    "GETEX",
    RedisOperation.WRITE,
    [RespCode.BULK, RespCode.NULL],
)
fun interface GetExCommand : RedisCommandSpec<String> {
    suspend fun encode(
        key: String,
        vararg expiration: GetExOption,
    ): CommandRequest
}
