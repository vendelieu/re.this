package eu.vendeli.rethis.api.spec.commands.sentinel

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.*

@RedisCommand("SENTINEL PENDING-SCRIPTS", RedisOperation.READ, [RespCode.ARRAY])
fun interface SentinelPendingScriptsCommand : RedisCommandSpec<List<RType>> {
    suspend fun encode(): CommandRequest
}
