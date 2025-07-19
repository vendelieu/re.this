package eu.vendeli.rethis.api.spec.commands.geospatial

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.decoders.geospatial.GeoPosDecoder
import eu.vendeli.rethis.api.spec.common.response.geospatial.GeoPosition
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("GEOPOS", RedisOperation.READ, [RespCode.ARRAY])
@RedisMeta.CustomCodec(decoder = GeoPosDecoder::class)
fun interface GeoPosCommand : RedisCommandSpec<List<List<GeoPosition>?>> {
    suspend fun encode(
        key: String,
        vararg member: String
    ): CommandRequest
}
