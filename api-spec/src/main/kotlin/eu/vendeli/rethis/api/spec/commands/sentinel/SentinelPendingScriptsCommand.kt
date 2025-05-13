package eu.vendeli.rethis.api.spec.commands.sentinel

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("SENTINEL PENDING-SCRIPTS", RedisOperation.READ, [RespCode.ARRAY])
fun interface SentinelPendingScriptsCommand : RedisCommandSpec<List<Map<String, RType>>> {
    suspend fun encode(): CommandRequest<Nothing>
}
