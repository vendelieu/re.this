package eu.vendeli.rethis.api.spec.commands.cluster

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@Deprecated(
    message = "CLUSTER SLAVES is deprecated as of Redis 5.0.0. Use CLUSTER REPLICAS instead.",
)
@RedisCommand("CLUSTER SLAVES", RedisOperation.READ, [RespCode.ARRAY])
fun interface ClusterSlavesCommand : RedisCommandSpec<List<String>> {
    suspend fun encode(nodeId: String): CommandRequest
}
