package eu.vendeli.rethis.api.spec.commands.sortedset

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.UpdateStrategyOption
import eu.vendeli.rethis.api.spec.common.response.ZMember
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("ZADD", RedisOperation.WRITE, [RespCode.INTEGER], extensions = [ZMember::class, UpdateStrategyOption::class])
fun interface ZAddCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        @RedisKey key: String,
        @RedisOptional updateType: UpdateStrategyOption?,
        @RedisOptional ch: Boolean?,
        vararg members: ZMember
    ): CommandRequest<String>
}
