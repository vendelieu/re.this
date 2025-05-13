package eu.vendeli.rethis.api.spec.commands.geospatial

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.GeoAddOption
import eu.vendeli.rethis.api.spec.common.response.GeoMember
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("GEOADD", RedisOperation.WRITE, [RespCode.INTEGER], extensions = [GeoMember::class, GeoAddOption.UpsertMode::class])
fun interface GeoAddCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        @RedisKey key: String,
        vararg member: GeoMember,
        @RedisOptional upsertMode: GeoAddOption.UpsertMode?,
        @RedisOptional ch: Boolean?
    ): CommandRequest<String>
}
