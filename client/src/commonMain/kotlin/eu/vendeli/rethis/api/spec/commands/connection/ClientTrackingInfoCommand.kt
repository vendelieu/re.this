package eu.vendeli.rethis.api.spec.commands.connection

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.decoders.general.RTypeDecoder
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("CLIENT TRACKINGINFO", RedisOperation.READ, [RespCode.ARRAY, RespCode.MAP])
@RedisMeta.CustomCodec(decoder = RTypeDecoder::class)
fun interface ClientTrackingInfoCommand : RedisCommandSpec<RType> {
    suspend fun encode(): CommandRequest
}
