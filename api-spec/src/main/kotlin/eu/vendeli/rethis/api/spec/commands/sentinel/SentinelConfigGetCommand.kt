package eu.vendeli.rethis.api.spec.commands.sentinel

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("SENTINEL CONFIG GET", RedisOperation.READ, [RespCode.ARRAY, RespCode.MAP])
fun interface SentinelConfigGetCommand : RedisCommandSpec<Map<String, String>> {
    suspend fun encode(name: String): CommandRequest<Nothing>
}
