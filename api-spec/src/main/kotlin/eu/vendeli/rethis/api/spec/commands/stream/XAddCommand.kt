package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.request.common.FieldValue
import eu.vendeli.rethis.shared.request.stream.XAddOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand(
    "XADD",
    RedisOperation.WRITE,
    [RespCode.BULK, RespCode.NULL],
)
fun interface XAddCommand : RedisCommandSpec<String> {
    suspend fun encode(
        key: String,
        @RedisOption.Token("NOMKSTREAM") nomkstream: Boolean?,
        trim: XAddOption.Trim?,
        idSelector: XAddOption.Identifier,
        vararg data: FieldValue,
    ): CommandRequest
}
