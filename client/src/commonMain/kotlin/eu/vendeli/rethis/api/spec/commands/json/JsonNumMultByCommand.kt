package eu.vendeli.rethis.api.spec.commands.json

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.decoders.general.RTypeDecoder
import eu.vendeli.rethis.shared.types.*
import eu.vendeli.rethis.utils.JSON_DEFAULT_PATH

@Deprecated(
    message = "JSON.NUMMULTBY is deprecated as of RedisJSON 2.0. " +
        "Multiplication is no longer supported; combine JSON.GET and JSON.SET if needed.",
)
@RedisCommand("JSON.NUMMULTBY", RedisOperation.WRITE, [RespCode.ARRAY])
@RedisMeta.CustomCodec(decoder = RTypeDecoder::class)
fun interface JsonNumMultByCommand : RedisCommandSpec<RType> {
    suspend fun encode(
        key: String,
        value: Double,
        @RedisMeta.Default("\"$JSON_DEFAULT_PATH\"") path: String,
    ): CommandRequest
}
