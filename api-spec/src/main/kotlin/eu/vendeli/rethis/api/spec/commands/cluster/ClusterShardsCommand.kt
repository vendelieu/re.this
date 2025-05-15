package eu.vendeli.rethis.api.spec.commands.cluster

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("CLUSTER SHARDS", RedisOperation.READ, [RespCode.ARRAY])
fun interface ClusterShardsCommand : RedisCommandSpec<List<Map<String, RType>>> {
    suspend fun encode(): CommandRequest<Nothing>
}
