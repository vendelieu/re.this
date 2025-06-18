package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.request.stream.Exactement
import eu.vendeli.rethis.api.spec.common.request.stream.TrimmingStrategy
import eu.vendeli.rethis.api.spec.common.request.stream.XOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("XTRIM", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface XTrimCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        key: String,
        threshold: String,
        strategy: TrimmingStrategy,
        operator: Exactement?,
        trim: XOption.Limit?
    ): CommandRequest
}
