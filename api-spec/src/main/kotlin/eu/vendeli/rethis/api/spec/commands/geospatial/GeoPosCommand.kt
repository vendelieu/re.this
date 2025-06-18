package eu.vendeli.rethis.api.spec.commands.geospatial

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.response.GeoPosition
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("GEOPOS", RedisOperation.READ, [RespCode.ARRAY])
fun interface GeoPosCommand : RedisCommandSpec<List<List<GeoPosition>?>> {
    suspend fun encode(
        key: String,
        vararg member: String
    ): CommandRequest
}
