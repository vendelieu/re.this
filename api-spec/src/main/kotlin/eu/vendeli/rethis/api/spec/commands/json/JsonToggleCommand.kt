package eu.vendeli.rethis.api.spec.commands.json

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.decoders.general.RTypeDecoder
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("JSON.TOGGLE", RedisOperation.WRITE, [RespCode.ARRAY])
@RedisMeta.CustomCodec(decoder = RTypeDecoder::class)
fun interface JsonToggleCommand : RedisCommandSpec<RType> {
    suspend fun encode(
        key: String,
        path: String
    ): CommandRequest
}
