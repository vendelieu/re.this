package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.XAddOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand(
    "XADD",
    RedisOperation.WRITE,
    [RespCode.BULK, RespCode.NULL],
    extensions = [XAddOption.NOMKSTREAM::class, XAddOption.Trim::class, XAddOption.Identifier::class],
)
fun interface XAddCommand : RedisCommandSpec<String> {
    suspend fun encode(
        @RedisKey key: String,
        @RedisOptional nomkstream: XAddOption.NOMKSTREAM?,
        @RedisOptional trim: XAddOption.Trim?,
        id: XAddOption.Identifier,
        vararg entry: Pair<String, String>,
    ): CommandRequest<String>
}
