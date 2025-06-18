package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.request.common.FieldValue
import eu.vendeli.rethis.api.spec.common.request.stream.XAddOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

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
        vararg entry: FieldValue,
    ): CommandRequest
}
