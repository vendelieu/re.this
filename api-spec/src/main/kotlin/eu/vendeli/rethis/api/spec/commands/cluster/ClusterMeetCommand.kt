package eu.vendeli.rethis.api.spec.commands.cluster

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.annotations.RedisOptional
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("CLUSTER MEET", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface ClusterMeetCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(ip: String, port: Long, @RedisOptional clusterBusPort: Long?): CommandRequest
}
