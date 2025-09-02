package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.decoders.sortedset.ZPopResultDecoder
import eu.vendeli.rethis.shared.response.stream.ZPopResult
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand(
    "BZPOPMIN",
    RedisOperation.WRITE,
    [RespCode.ARRAY, RespCode.NULL],
    isBlocking = true,
)
@RedisMeta.CustomCodec(decoder = ZPopResultDecoder::class)
fun interface BzPopMinCommand : RedisCommandSpec<ZPopResult> {
    suspend fun encode(
        timeout: Double,
        vararg key: String,
    ): CommandRequest
}
