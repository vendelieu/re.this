package eu.vendeli.rethis.api.spec.commands.geospatial

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.request.geospatial.CenterPoint
import eu.vendeli.rethis.shared.request.geospatial.Shape
import eu.vendeli.rethis.shared.response.geospatial.GeoSort
import eu.vendeli.rethis.shared.types.*

@RedisCommand(
    "GEOSEARCH",
    RedisOperation.READ,
    [RespCode.ARRAY],
)
fun interface GeoSearchCommand : RedisCommandSpec<List<RType>> {
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
