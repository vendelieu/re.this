package eu.vendeli.rethis.api.spec.commands.hash

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("HGETALL", RedisOperation.READ, [RespCode.ARRAY, RespCode.MAP])
fun interface HGetAllCommand : RedisCommandSpec<Map<String, String?>> {
    suspend fun encode(key: String): CommandRequest
}
