package eu.vendeli.rethis.api.spec.commands.cluster

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.*

@RedisCommand("CLUSTER LINKS", RedisOperation.READ, [RespCode.ARRAY])
fun interface ClusterLinksCommand : RedisCommandSpec<List<RType>> {
    suspend fun encode(): CommandRequest
}
