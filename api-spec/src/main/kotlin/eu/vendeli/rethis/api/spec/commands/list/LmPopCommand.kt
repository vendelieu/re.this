package eu.vendeli.rethis.api.spec.commands.list

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.response.MPopResult
import eu.vendeli.rethis.api.spec.common.response.MoveDirection
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("LMPOP", RedisOperation.WRITE, [RespCode.ARRAY, RespCode.NULL], extensions = [MoveDirection::class])
fun interface LmPopCommand : RedisCommandSpec<List<MPopResult>> {
    suspend fun encode(
        direction: MoveDirection,
        @RedisKey @RedisMeta.WithSizeParam("numkeys") vararg key: String,
        @RedisOptional count: Long?,
    ): CommandRequest<List<String>>
}
