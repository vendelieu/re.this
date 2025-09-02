package eu.vendeli.rethis.api.spec.commands.list

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.decoders.common.LPopDecoder
import eu.vendeli.rethis.shared.response.common.PopResult
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("BLPOP", RedisOperation.WRITE, [RespCode.ARRAY, RespCode.NULL], isBlocking = true)
@RedisMeta.CustomCodec(decoder = LPopDecoder::class)
fun interface BlPopCommand : RedisCommandSpec<PopResult> {
    suspend fun encode(
        vararg key: String,
        timeout: Double
    ): CommandRequest
}
