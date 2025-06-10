package eu.vendeli.rethis.api.spec.commands.cluster

import eu.vendeli.rethis.api.spec.common.annotations.RedisCommand
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisCommandSpec
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode

@RedisCommand("CLUSTER GETKEYSINSLOT", RedisOperation.READ, [RespCode.ARRAY])
fun interface ClusterGetKeysInSlotCommand : RedisCommandSpec<List<String>> {
    suspend fun encode(slot: Long, count: Long): CommandRequest
}
