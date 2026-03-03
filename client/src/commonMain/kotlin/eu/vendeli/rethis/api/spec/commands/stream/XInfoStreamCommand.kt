package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.types.*

@RedisCommand("XINFO STREAM", RedisOperation.READ, [RespCode.MAP, RespCode.ARRAY])
fun interface XInfoStreamCommand : RedisCommandSpec<Map<String, RType>> {
    suspend fun encode(
        key: String,
        @RedisOption.Token("FULL") full: Boolean?,
        @RedisOption.Token("COUNT") count: Long?,
    ): CommandRequest
}
