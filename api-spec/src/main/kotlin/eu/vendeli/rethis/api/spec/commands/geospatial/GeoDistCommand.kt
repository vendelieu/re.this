package eu.vendeli.rethis.api.spec.commands.geospatial

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.response.GeoUnit
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("GEODIST", RedisOperation.READ, [RespCode.BULK, RespCode.NULL], extensions = [GeoUnit::class])
fun interface GeoDistCommand : RedisCommandSpec<Double> {
    suspend fun encode(
        key: String,
        member1: String,
        member2: String,
        @RedisOptional unit: GeoUnit?
    ): CommandRequest
}
