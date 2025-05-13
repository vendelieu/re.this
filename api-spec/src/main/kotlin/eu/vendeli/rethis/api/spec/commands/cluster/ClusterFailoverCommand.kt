package eu.vendeli.rethis.api.spec.commands.cluster

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.request.ClusterFailoverOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("CLUSTER FAILOVER", RedisOperation.WRITE, [RespCode.SIMPLE_STRING], extensions = [ClusterFailoverOption::class])
fun interface ClusterFailoverCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(@RedisOptional option: ClusterFailoverOption?): CommandRequest<Nothing>
}
