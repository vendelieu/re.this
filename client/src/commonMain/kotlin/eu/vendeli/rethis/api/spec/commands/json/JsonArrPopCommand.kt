package eu.vendeli.rethis.api.spec.commands.json

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.decoders.general.RTypeDecoder
import eu.vendeli.rethis.shared.types.*

@RedisCommand("JSON.ARRPOP", RedisOperation.WRITE, [RespCode.ARRAY])
@RedisMeta.CustomCodec(decoder = RTypeDecoder::class)
fun interface JsonArrPopCommand : RedisCommandSpec<RType> {
    suspend fun encode(
        key: String,
        path: String?,
        index: Long?
    ): CommandRequest
}
