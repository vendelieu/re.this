package eu.vendeli.rethis.api.spec.commands.tdigest

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.decoders.general.RTypeDecoder
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("TDIGEST.MAX", RedisOperation.READ, [RespCode.BULK, RespCode.DOUBLE, RespCode.SIMPLE_STRING])
@RedisMeta.CustomCodec(decoder = RTypeDecoder::class)
fun interface TDigestMaxCommand : RedisCommandSpec<RType> {
    suspend fun encode(key: String): CommandRequest
}
