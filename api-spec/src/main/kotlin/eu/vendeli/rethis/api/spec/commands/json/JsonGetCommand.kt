package eu.vendeli.rethis.api.spec.commands.json

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("JSON.GET", RedisOperation.READ, [RespCode.BULK, RespCode.NULL])
fun interface JsonGetCommand : RedisCommandSpec<String> {
    suspend fun encode(
        @RedisKey key: String,
        @RedisOptional vararg path: String?
    ): CommandRequest<String>
}
