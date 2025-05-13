package eu.vendeli.rethis.api.spec.commands.sentinel

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("SENTINEL REPLICAS", RedisOperation.READ, [RespCode.ARRAY])
fun interface SentinelReplicasCommand : RedisCommandSpec<List<Map<String, RType>>> {
    suspend fun encode(masterName: String): CommandRequest<Nothing>
}
