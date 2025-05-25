package eu.vendeli.rethis.api.spec.commands.stream

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.Exactement
import eu.vendeli.rethis.api.spec.common.request.TrimmingStrategy
import eu.vendeli.rethis.api.spec.common.request.XOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("XTRIM", RedisOperation.WRITE, [RespCode.INTEGER], extensions = [TrimmingStrategy::class, Exactement::class, XOption.Limit::class])
fun interface XTrimCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        @RedisKey key: String,
        threshold: String,
        strategy: TrimmingStrategy,
        @RedisOptional operator: Exactement?,
        @RedisOptional trim: XOption.Limit?
    ): CommandRequest<String>
}
