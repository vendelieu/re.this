package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.request.stream.Exactement
import eu.vendeli.rethis.shared.request.stream.TrimmingStrategy
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("XTRIM", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface XTrimCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        key: String,
        threshold: String,
        strategy: TrimmingStrategy,
        operator: Exactement?,
        @RedisOption.Token("LIMIT") count: Long?,
    ): CommandRequest
}
