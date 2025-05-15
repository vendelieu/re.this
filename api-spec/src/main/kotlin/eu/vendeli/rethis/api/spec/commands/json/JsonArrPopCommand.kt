package eu.vendeli.rethis.api.spec.commands.json

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("JSON.ARRPOP", RedisOperation.WRITE, [RespCode.ARRAY])
fun interface JsonArrPopCommand : RedisCommandSpec<RType> {
    suspend fun encode(
        @RedisKey key: String,
        path: String,
        @RedisOptional index: Long?
    ): CommandRequest<String>
}
