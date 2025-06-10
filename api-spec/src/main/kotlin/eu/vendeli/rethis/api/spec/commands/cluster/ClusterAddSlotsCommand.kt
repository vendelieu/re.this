package eu.vendeli.rethis.api.spec.commands.cluster

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("CLUSTER ADDSLOTS", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface ClusterAddSlotsCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(vararg slot: Long): CommandRequest
}
