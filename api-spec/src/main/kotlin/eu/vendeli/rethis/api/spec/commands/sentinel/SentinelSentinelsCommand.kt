package eu.vendeli.rethis.api.spec.commands.sentinel

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("SENTINEL SENTINELS", RedisOperation.READ, [RespCode.ARRAY])
fun interface SentinelSentinelsCommand : RedisCommandSpec<List<Map<String, RType>>> {
    suspend fun encode(masterName: String): CommandRequest
}
