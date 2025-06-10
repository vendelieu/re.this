package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
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
    extensions = [XAddOption.Trim::class, XAddOption.Identifier::class, FieldValue::class],
)
fun interface XAddCommand : RedisCommandSpec<String> {
    suspend fun encode(
        key: String,
        @RedisOptional @RedisOption.Token("NOMKSTREAM") nomkstream: Boolean?,
        @RedisOptional trim: XAddOption.Trim?,
        idSelector: XAddOption.Identifier,
        vararg entry: FieldValue,
    ): CommandRequest
}
