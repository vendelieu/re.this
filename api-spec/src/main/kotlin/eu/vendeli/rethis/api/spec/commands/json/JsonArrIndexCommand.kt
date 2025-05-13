package eu.vendeli.rethis.api.spec.commands.json

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("JSON.ARRINDEX", RedisOperation.WRITE, [RespCode.INTEGER])
fun interface JsonArrIndexCommand : RedisCommandSpec<Long> {
    suspend fun encode(
        @RedisKey key: String,
        path: String,
        value: String
    ): CommandRequest<String>
}
