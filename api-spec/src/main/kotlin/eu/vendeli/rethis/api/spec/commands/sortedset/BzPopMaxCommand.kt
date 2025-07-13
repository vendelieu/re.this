package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.decoders.sortedset.ZPopResultDecoder
import eu.vendeli.rethis.api.spec.common.response.ZPopResult
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("BZPOPMAX", RedisOperation.WRITE, [RespCode.ARRAY, RespCode.NULL], isBlocking = true)
@RedisMeta.CustomCodec(decoder = ZPopResultDecoder::class)
fun interface BzPopMaxCommand : RedisCommandSpec<ZPopResult> {
    suspend fun encode(
        timeout: Double,
        vararg key: String
    ): CommandRequest
}
