package eu.vendeli.rethis.api.spec.commands.geospatial

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.annotations.RedisOption
import eu.vendeli.rethis.shared.request.geospatial.GeoAddOption
import eu.vendeli.rethis.shared.response.geospatial.GeoMember
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand(
    "GEOADD",
    RedisOperation.WRITE,
    [RespCode.INTEGER],
)
fun interface GeoAddCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        key: String,
        vararg data: GeoMember,
        condition: GeoAddOption.UpsertMode?,
        @RedisOption.Token("CH") change: Boolean?,
    ): CommandRequest
}
