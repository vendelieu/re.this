package eu.vendeli.rethis.api.spec.commands.json

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.decoders.general.RTypeDecoder
import eu.vendeli.rethis.shared.types.*
import eu.vendeli.rethis.utils.JSON_DEFAULT_PATH

@RedisCommand("JSON.ARRPOP", RedisOperation.WRITE, [RespCode.ARRAY])
@RedisMeta.CustomCodec(decoder = RTypeDecoder::class)
fun interface JsonArrPopCommand : RedisCommandSpec<RType> {
    suspend fun encode(
        key: String,
        index: Long?,
        @RedisMeta.Default("\"$JSON_DEFAULT_PATH\"") path: String?,
    ): CommandRequest
}
