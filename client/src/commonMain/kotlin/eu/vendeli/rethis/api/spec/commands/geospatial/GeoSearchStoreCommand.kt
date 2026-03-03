package eu.vendeli.rethis.api.spec.commands.geospatial

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.request.geospatial.CenterPoint
import eu.vendeli.rethis.shared.request.geospatial.Shape
import eu.vendeli.rethis.shared.response.geospatial.GeoSort
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand(
    "GEOSEARCHSTORE",
    RedisOperation.WRITE,
    [RespCode.INTEGER],
)
fun interface GeoSearchStoreCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        destination: String,
        source: String,
        from: CenterPoint,
        by: Shape,
        order: GeoSort?,
        @RedisOption.Token("COUNT") count: Long?,
        @RedisOption.Token("ANY") any: Boolean?,
        @RedisOption.Token("STOREDIST") storedist: Boolean?,
    ): CommandRequest
}
