package eu.vendeli.rethis.api.spec.commands.json

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.decoders.general.RTypeDecoder
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("JSON.OBJLEN", RedisOperation.READ, [RespCode.ARRAY])
@RedisMeta.CustomCodec(decoder = RTypeDecoder::class)
fun interface JsonObjLenCommand : RedisCommandSpec<RType> {
    suspend fun encode(
        key: String,
        path: String?
    ): CommandRequest
}
