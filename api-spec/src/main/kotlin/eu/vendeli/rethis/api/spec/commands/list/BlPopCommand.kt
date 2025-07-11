package eu.vendeli.rethis.api.spec.commands.list

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import eu.vendeli.rethis.api.spec.common.response.PopResult
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("BLPOP", RedisOperation.WRITE, [RespCode.ARRAY, RespCode.NULL], isBlocking = true)
@RedisMeta.CustomCodec(decoder = ResponseDecoder::class) // todo add
fun interface BlPopCommand : RedisCommandSpec<PopResult> {
    suspend fun encode(
        vararg key: String,
        timeout: Double
    ): CommandRequest
}
