package eu.vendeli.rethis.api.spec.commands.list

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.list.LPosOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("LPOS", RedisOperation.READ, [RespCode.INTEGER, RespCode.NULL], extensions = [LPosOption.CommonOption::class])
fun interface LPosCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        @RedisKey key: String,
        element: String,
        @RedisOptional vararg option: LPosOption.CommonOption
    ): CommandRequest<String>
}
