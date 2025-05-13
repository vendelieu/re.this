package eu.vendeli.rethis.api.spec.commands.cluster

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("CLUSTER REPLICAS", RedisOperation.READ, [RespCode.ARRAY])
fun interface ClusterReplicasCommand : RedisCommandSpec<List<String>> {
    suspend fun encode(nodeId: String): CommandRequest<Nothing>
}
