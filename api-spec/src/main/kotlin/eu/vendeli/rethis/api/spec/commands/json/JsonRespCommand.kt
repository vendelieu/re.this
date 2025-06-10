package eu.vendeli.rethis.api.spec.commands.json

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("JSON.RESP", RedisOperation.READ, [RespCode.ARRAY])
fun interface JsonRespCommand : RedisCommandSpec<List<RType>> {
    suspend fun encode(
        key: String,
        @RedisOptional path: String?
    ): CommandRequest
}
