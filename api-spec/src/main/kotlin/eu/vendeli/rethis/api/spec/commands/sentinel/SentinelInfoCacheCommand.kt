package eu.vendeli.rethis.api.spec.commands.sentinel

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("SENTINEL INFO-CACHE", RedisOperation.READ, [RespCode.MAP])
fun interface SentinelInfoCacheCommand : RedisCommandSpec<Map<String, String>> {
    suspend fun encode(): CommandRequest
}
