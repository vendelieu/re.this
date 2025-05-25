package eu.vendeli.rethis.api.spec.commands.generic

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.CopyOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("COPY", RedisOperation.WRITE, [RespCode.INTEGER], extensions = [CopyOption::class])
fun interface CopyCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(
        @RedisKey source: String,
        @RedisKey destination: String,
        @RedisOptional vararg option: CopyOption
    ): CommandRequest<List<String>>
}
