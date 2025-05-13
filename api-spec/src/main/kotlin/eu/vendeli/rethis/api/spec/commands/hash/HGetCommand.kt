package eu.vendeli.rethis.api.spec.commands.hash

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisKey
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("HGET", RedisOperation.READ, [RespCode.BULK, RespCode.NULL])
fun interface HGetCommand : RedisCommandSpec<String> {
    suspend fun encode(
        @RedisKey key: String,
        field: String
    ): CommandRequest<String>
}
