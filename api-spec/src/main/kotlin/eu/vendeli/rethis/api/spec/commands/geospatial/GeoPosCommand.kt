package eu.vendeli.rethis.api.spec.commands.geospatial

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisMeta
import eu.vendeli.rethis.shared.decoders.geospatial.GeoPosDecoder
import eu.vendeli.rethis.shared.response.geospatial.GeoPosition
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("GEOPOS", RedisOperation.READ, [RespCode.ARRAY])
@RedisMeta.CustomCodec(decoder = GeoPosDecoder::class)
fun interface GeoPosCommand : RedisCommandSpec<List<List<GeoPosition>?>> {
    suspend fun encode(
        key: String,
        vararg member: String
    ): CommandRequest
}
