package eu.vendeli.rethis.api.spec.commands.string

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.request.string.GetExOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode
import kotlinx.io.bytestring.ByteString

@RedisCommand(
    "GETEX",
    RedisOperation.WRITE,
    [RespCode.BULK, RespCode.NULL],
)
fun interface GetExBSCommand : RedisCommandSpec<ByteString> {
    suspend fun encode(
        key: String,
        vararg expiration: GetExOption,
    ): CommandRequest
}
