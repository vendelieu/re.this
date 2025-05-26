package eu.vendeli.rethis.api.spec.commands.geospatial

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
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
    extensions = [CenterPoint::class, Shape::class, GeoSort::class],
)
fun interface GeoSearchCommand : RedisCommandSpec<List<GeoSearchResult>> {
    suspend fun encode(
        @RedisKey key: String,
        from: CenterPoint,
        by: Shape,
        @RedisOptional @RedisOption.Token("WITHCOORD") withCoord: Boolean?,
        @RedisOptional @RedisOption.Token("WITHDIST") withDist: Boolean?,
        @RedisOptional @RedisOption.Token("WITHHASH") withHash: Boolean?,
        @RedisOptional @RedisOption.Token("COUNT") count: Long?,
        @RedisOptional @RedisOption.Token("ANY") any: Boolean?,
        @RedisOptional order: GeoSort?,
    ): CommandRequest<String>
}
