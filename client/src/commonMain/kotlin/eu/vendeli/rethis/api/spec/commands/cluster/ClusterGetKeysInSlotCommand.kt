package eu.vendeli.rethis.api.spec.commands.cluster

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("CLUSTER GETKEYSINSLOT", RedisOperation.READ, [RespCode.ARRAY])
fun interface ClusterGetKeysInSlotCommand : RedisCommandSpec<List<String>> {
    suspend fun encode(slot: Long, count: Long): CommandRequest
}
