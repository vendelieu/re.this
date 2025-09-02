package eu.vendeli.rethis.api.spec.commands.geospatial

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.response.geospatial.GeoUnit
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("GEODIST", RedisOperation.READ, [RespCode.BULK, RespCode.NULL])
fun interface GeoDistCommand : RedisCommandSpec<Double> {
    suspend fun encode(
        key: String,
        member1: String,
        member2: String,
        unit: GeoUnit?
    ): CommandRequest
}
