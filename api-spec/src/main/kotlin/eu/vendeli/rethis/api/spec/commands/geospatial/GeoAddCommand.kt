package eu.vendeli.rethis.api.spec.commands.geospatial

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.request.geospatial.GeoAddOption
import eu.vendeli.rethis.api.spec.common.response.geospatial.GeoMember
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

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
