package eu.vendeli.rethis.api.spec.commands.cluster

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("CLUSTER SLOTS", RedisOperation.READ, [RespCode.ARRAY])
fun interface ClusterSlotsCommand : RedisCommandSpec<List<RType>> {
    suspend fun encode(): CommandRequest
}
