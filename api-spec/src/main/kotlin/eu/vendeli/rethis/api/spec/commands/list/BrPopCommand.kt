package eu.vendeli.rethis.api.spec.commands.list

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.response.PopResult
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("BRPOP", RedisOperation.WRITE, [RespCode.ARRAY, RespCode.NULL], isBlocking = true)
fun interface BrPopCommand : RedisCommandSpec<List<PopResult>> {
    suspend fun encode(
        @RedisKey vararg key: String,
        timeout: Double
    ): CommandRequest<List<String>>
}
