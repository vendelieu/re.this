package eu.vendeli.rethis.api.spec.commands.json

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("JSON.MGET", RedisOperation.READ, [RespCode.ARRAY])
fun interface JsonMGetCommand : RedisCommandSpec<List<String?>> {
    suspend fun encode(
        path: String,
        @RedisKey vararg key: String
    ): CommandRequest<List<String>>
}
