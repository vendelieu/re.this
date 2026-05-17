package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.request.stream.XNackMode
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("XNACK", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface XNackCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        key: String,
        group: String,
        mode: XNackMode,
        @RedisOption.Token("IDS") @RedisMeta.WithSizeParam("numids") ids: List<String>,
        @RedisOption.Token("RETRYCOUNT") count: Long?,
        @RedisOption.Token("FORCE") force: Boolean?,
    ): CommandRequest
}
