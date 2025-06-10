package eu.vendeli.rethis.api.spec.commands.cluster

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.*

@RedisCommand("CLUSTER LINKS", RedisOperation.READ, [RespCode.ARRAY])
fun interface ClusterLinksCommand : RedisCommandSpec<List<Map<String, RType>>> {
    suspend fun encode(): CommandRequest
}
