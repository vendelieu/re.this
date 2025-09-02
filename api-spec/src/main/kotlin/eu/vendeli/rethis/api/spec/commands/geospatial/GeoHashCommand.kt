package eu.vendeli.rethis.api.spec.commands.geospatial

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("GEOHASH", RedisOperation.READ, [RespCode.ARRAY])
fun interface GeoHashCommand : RedisCommandSpec<List<String>> {
    suspend fun encode(
        key: String,
        vararg member: String
    ): CommandRequest
}
