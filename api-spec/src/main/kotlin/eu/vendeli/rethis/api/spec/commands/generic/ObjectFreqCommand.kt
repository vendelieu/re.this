package eu.vendeli.rethis.api.spec.commands.generic

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.decoders.general.RTypeDecoder
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("OBJECT FREQ", RedisOperation.READ, [RespCode.INTEGER, RespCode.NULL])
@RedisMeta.CustomCodec(decoder = RTypeDecoder::class)
fun interface ObjectFreqCommand : RedisCommandSpec<RType> {
    suspend fun encode(key: String): CommandRequest
}
