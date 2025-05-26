package eu.vendeli.rethis.api.spec.commands.geospatial

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.geospatial.CenterPoint
import eu.vendeli.rethis.api.spec.common.request.geospatial.Shape
import eu.vendeli.rethis.api.spec.common.response.GeoSort
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand(
    "GEOSEARCHSTORE",
    RedisOperation.WRITE,
    [RespCode.INTEGER],
    extensions = [CenterPoint::class, Shape::class, GeoSort::class],
)
fun interface GeoSearchStoreCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        @RedisKey destination: String,
        @RedisKey source: String,
        from: CenterPoint,
        by: Shape,
        @RedisOptional order: GeoSort?,
        @RedisOptional @RedisOption.Token("COUNT") count: Long?,
        @RedisOptional @RedisOption.Token("ANY") any: Boolean?,
        @RedisOptional @RedisOption.Token("STOREDIST") storedist: Boolean?,
    ): CommandRequest<List<String>>
}
