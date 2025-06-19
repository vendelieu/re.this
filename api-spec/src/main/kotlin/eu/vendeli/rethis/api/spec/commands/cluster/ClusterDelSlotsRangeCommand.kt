package eu.vendeli.rethis.api.spec.commands.cluster

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.request.cluster.SlotRange
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("CLUSTER DELSLOTSRANGE", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface ClusterDelSlotsRangeCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(vararg range: SlotRange): CommandRequest
}
