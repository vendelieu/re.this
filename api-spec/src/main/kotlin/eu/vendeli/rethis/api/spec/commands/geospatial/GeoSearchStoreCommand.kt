package eu.vendeli.rethis.api.spec.commands.geospatial

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.CenterPoint
import eu.vendeli.rethis.api.spec.common.request.Shape
import eu.vendeli.rethis.api.spec.common.response.GeoSort
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("GEOSEARCHSTORE", RedisOperation.WRITE, [RespCode.INTEGER], extensions = [CenterPoint::class, Shape::class, GeoSort::class])
fun interface GeoSearchStoreCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        @RedisKey destination: String,
        @RedisKey source: String,
        center: CenterPoint,
        shape: Shape,
        @RedisOptional sort: GeoSort?,
        @RedisOptional count: Long?,
        @RedisOptional any: Boolean?,
        @RedisOptional storedist: Boolean?
    ): CommandRequest<List<String>>
}
