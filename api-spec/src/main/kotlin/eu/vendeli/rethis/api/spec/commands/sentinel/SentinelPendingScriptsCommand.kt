package eu.vendeli.rethis.api.spec.commands.sentinel

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("SENTINEL PENDING-SCRIPTS", RedisOperation.READ, [RespCode.ARRAY])
fun interface SentinelPendingScriptsCommand : RedisCommandSpec<List<Map<String, RType>>> {
    suspend fun encode(): CommandRequest
}
