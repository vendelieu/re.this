package eu.vendeli.rethis.api.spec.commands.json

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("JSON.DEL", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface JsonDelCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        key: String,
        @RedisOptional path: String?
    ): CommandRequest
}
