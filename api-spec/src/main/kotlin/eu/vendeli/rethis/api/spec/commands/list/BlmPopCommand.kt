package eu.vendeli.rethis.api.spec.commands.list

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.annotations.RedisOption
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.response.MPopResult
import eu.vendeli.rethis.api.spec.common.response.MoveDirection
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand(
    "BLMPOP",
    RedisOperation.WRITE,
    [RespCode.ARRAY, RespCode.NULL],
    isBlocking = true,
    extensions = [MoveDirection::class],
)
fun interface BlmPopCommand : RedisCommandSpec<List<MPopResult>> {
    suspend fun encode(
        timeout: Double,
        @RedisKey @RedisMeta.WithSizeParam("numkeys") vararg key: String,
        where: MoveDirection,
        @RedisOptional @RedisOption.Token("COUNT") count: Long?,
    ): CommandRequest<List<String>>
}
