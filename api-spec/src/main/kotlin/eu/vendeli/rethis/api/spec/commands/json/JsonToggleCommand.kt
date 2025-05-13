package eu.vendeli.rethis.api.spec.commands.json

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("JSON.TOGGLE", RedisOperation.WRITE, [RespCode.ARRAY])
fun interface JsonToggleCommand : RedisCommandSpec<List<Long>> {
    suspend fun encode(
        @RedisKey key: String,
        path: String
    ): CommandRequest<String>
}
