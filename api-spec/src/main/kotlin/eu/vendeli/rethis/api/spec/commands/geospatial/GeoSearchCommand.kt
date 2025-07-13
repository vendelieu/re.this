package eu.vendeli.rethis.api.spec.commands.geospatial

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import eu.vendeli.rethis.api.spec.common.request.geospatial.CenterPoint
import eu.vendeli.rethis.api.spec.common.request.geospatial.Shape
import eu.vendeli.rethis.api.spec.common.response.GeoSearchResult
import eu.vendeli.rethis.api.spec.common.response.GeoSort
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand(
    "GEOSEARCH",
    RedisOperation.READ,
    [RespCode.ARRAY],
)
@RedisMeta.CustomCodec(decoder = ResponseDecoder::class) // todo add
fun interface GeoSearchCommand : RedisCommandSpec<List<GeoSearchResult>> {
    suspend fun encode(
        key: String,
        from: CenterPoint,
        by: Shape,
        @RedisOption.Token("WITHCOORD") @RedisOption.Name("withcoord") withCoord: Boolean?,
        @RedisOption.Token("WITHDIST") @RedisOption.Name("withdist") withDist: Boolean?,
        @RedisOption.Token("WITHHASH") @RedisOption.Name("withhash") withHash: Boolean?,
        @RedisOption.Token("COUNT") count: Long?,
        @RedisOption.Token("ANY") any: Boolean?,
        order: GeoSort?,
    ): CommandRequest
}
