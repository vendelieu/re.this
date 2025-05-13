package eu.vendeli.rethis.api.spec.commands.cluster

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("CLUSTER SHARDS", RedisOperation.READ, [RespCode.ARRAY])
fun interface ClusterShardsCommand : RedisCommandSpec<List<Map<String, RType>>> {
    suspend fun encode(): CommandRequest<Nothing>
}
