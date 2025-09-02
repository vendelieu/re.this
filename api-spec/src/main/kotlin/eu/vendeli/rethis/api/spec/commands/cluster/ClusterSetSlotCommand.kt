package eu.vendeli.rethis.api.spec.commands.cluster

import eu.vendeli.rethis.shared.annotations.RedisCommand
import eu.vendeli.rethis.shared.request.cluster.ClusterSetSlotOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisCommandSpec
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode

@RedisCommand("CLUSTER SETSLOT", RedisOperation.WRITE, [RespCode.SIMPLE_STRING])
fun interface ClusterSetSlotCommand : RedisCommandSpec<Boolean> {
    suspend fun encode(slot: Long, subcommand: ClusterSetSlotOption): CommandRequest
}
